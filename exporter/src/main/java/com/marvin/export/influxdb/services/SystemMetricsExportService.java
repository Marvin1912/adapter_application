package com.marvin.export.influxdb.services;

import com.influxdb.query.FluxRecord;
import com.marvin.export.influxdb.AbstractInfluxExport;
import com.marvin.export.influxdb.InfluxQueryBuilder;
import com.marvin.export.influxdb.dto.SystemMetricsDTO;
import com.marvin.export.influxdb.handlers.DataTypeHandler;
import com.marvin.export.influxdb.mappings.MeasurementMappings;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

/**
 * Export service for system metrics data from the system_metrics bucket.
 * Handles CPU, memory, disk, network, and system performance metrics from host home-server.
 */
@Service
public class SystemMetricsExportService extends AbstractInfluxExport<SystemMetricsDTO> {

    private static final String BUCKET_NAME = "system_metrics";

    @Override
    protected String getBucketName() {
        return BUCKET_NAME;
    }

    @Override
    protected String buildQuery(Instant startTime, Instant endTime) {
        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurements(
                    // System metrics measurements we want to export
                    "cpu", "mem", "system", "disk", "diskio", "net", "processes", "swap"
                )
                .keepOriginalColumns(true)
                .sort("desc") // Most recent first
                .build();
    }

    @Override
    protected Optional<SystemMetricsDTO> convertRecord(FluxRecord record) {
        try {
            // Use the DataTypeHandler to convert the record
            final Optional<?> converted = DataTypeHandler.convertRecord(record, BUCKET_NAME);
            if (converted.isPresent() && converted.get() instanceof SystemMetricsDTO dto) {
                // Validate the DTO using DataTypeHandler
                if (DataTypeHandler.validateDTO(dto, BUCKET_NAME)) {
                    return Optional.of(dto);
                } else {
                    LOGGER.debug("SystemMetricsDTO validation failed for record: {}", record);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to convert system metrics record: {}", record, e);
        }
        return Optional.empty();
    }

    @Override
    protected String getDataTypeDescription() {
        return "System performance metrics (CPU, memory, disk, network, processes) from host home-server";
    }

    /**
     * Builds a query for CPU-specific metrics only.
     */
    public String buildCpuMetricsQuery(Instant startTime, Instant endTime) {
        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurement("cpu")
                .fields(MeasurementMappings.SystemMetricsMappings.CPU_FIELDS.toArray(new String[0]))
                .tag("host", "home-server")
                .sort("desc")
                .build();
    }

    /**
     * Builds a query for memory-specific metrics only.
     */
    public String buildMemoryMetricsQuery(Instant startTime, Instant endTime) {
        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurement("mem")
                .fields(MeasurementMappings.SystemMetricsMappings.MEMORY_FIELDS.toArray(new String[0]))
                .tag("host", "home-server")
                .sort("desc")
                .build();
    }

    /**
     * Builds a query for disk-specific metrics only.
     */
    public String buildDiskMetricsQuery(Instant startTime, Instant endTime) {
        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurements("disk", "diskio")
                .fields(MeasurementMappings.SystemMetricsMappings.DISK_FIELDS.toArray(new String[0]))
                .tag("host", "home-server")
                .sort("desc")
                .build();
    }

    /**
     * Builds a query for network-specific metrics only.
     */
    public String buildNetworkMetricsQuery(Instant startTime, Instant endTime) {
        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurement("net")
                .fields(MeasurementMappings.SystemMetricsMappings.NETWORK_FIELDS.toArray(new String[0]))
                .tag("host", "home-server")
                .sort("desc")
                .build();
    }

    /**
     * Builds a query for system-level metrics only.
     */
    public String buildSystemMetricsQuery(Instant startTime, Instant endTime) {
        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurement("system")
                .fields(MeasurementMappings.SystemMetricsMappings.SYSTEM_FIELDS.toArray(new String[0]))
                .tag("host", "home-server")
                .sort("desc")
                .build();
    }

    /**
     * Builds a query for process-related metrics only.
     */
    public String buildProcessMetricsQuery(Instant startTime, Instant endTime) {
        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurement("processes")
                .fields(MeasurementMappings.SystemMetricsMappings.PROCESS_FIELDS.toArray(new String[0]))
                .tag("host", "home-server")
                .sort("desc")
                .build();
    }

    /**
     * Exports CPU metrics specifically.
     */
    public java.util.List<SystemMetricsDTO> exportCpuMetrics(Optional<Instant> startTime, Optional<Instant> endTime) {
        final Instant actualStartTime = startTime.orElse(getDefaultStartTime());
        final Instant actualEndTime = endTime.orElse(getDefaultEndTime());

        final String query = buildCpuMetricsQuery(actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports memory metrics specifically.
     */
    public java.util.List<SystemMetricsDTO> exportMemoryMetrics(Optional<Instant> startTime, Optional<Instant> endTime) {
        final Instant actualStartTime = startTime.orElse(getDefaultStartTime());
        final Instant actualEndTime = endTime.orElse(getDefaultEndTime());

        final String query = buildMemoryMetricsQuery(actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports disk metrics specifically.
     */
    public java.util.List<SystemMetricsDTO> exportDiskMetrics(Optional<Instant> startTime, Optional<Instant> endTime) {
        final Instant actualStartTime = startTime.orElse(getDefaultStartTime());
        final Instant actualEndTime = endTime.orElse(getDefaultEndTime());

        final String query = buildDiskMetricsQuery(actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports network metrics specifically.
     */
    public java.util.List<SystemMetricsDTO> exportNetworkMetrics(Optional<Instant> startTime, Optional<Instant> endTime) {
        final Instant actualStartTime = startTime.orElse(getDefaultStartTime());
        final Instant actualEndTime = endTime.orElse(getDefaultEndTime());

        final String query = buildNetworkMetricsQuery(actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports system-level metrics specifically.
     */
    public java.util.List<SystemMetricsDTO> exportSystemLevelMetrics(Optional<Instant> startTime, Optional<Instant> endTime) {
        final Instant actualStartTime = startTime.orElse(getDefaultStartTime());
        final Instant actualEndTime = endTime.orElse(getDefaultEndTime());

        final String query = buildSystemMetricsQuery(actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports process metrics specifically.
     */
    public java.util.List<SystemMetricsDTO> exportProcessMetrics(Optional<Instant> startTime, Optional<Instant> endTime) {
        final Instant actualStartTime = startTime.orElse(getDefaultStartTime());
        final Instant actualEndTime = endTime.orElse(getDefaultEndTime());

        final String query = buildProcessMetricsQuery(actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Helper method to execute a query and convert results.
     */
    private java.util.List<SystemMetricsDTO> executeQueryAndConvert(String query) {
        try {
            return executeQuery(query).stream()
                    .flatMap(table -> table.getRecords().stream())
                    .map(this::convertRecord)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();
        } catch (Exception e) {
            LOGGER.error("Failed to execute system metrics query: {}", query, e);
            throw new InfluxExportException("Failed to execute system metrics query", e);
        }
    }
}