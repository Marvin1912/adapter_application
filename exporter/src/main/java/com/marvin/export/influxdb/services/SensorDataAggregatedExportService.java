package com.marvin.export.influxdb.services;

import com.influxdb.query.FluxRecord;
import com.marvin.export.influxdb.AbstractInfluxExport;
import com.marvin.export.influxdb.InfluxQueryBuilder;
import com.marvin.export.influxdb.dto.SensorDataAggregatedDTO;
import com.marvin.export.influxdb.handlers.DataTypeHandler;
import com.marvin.export.influxdb.mappings.MeasurementMappings;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

/**
 * Export service for aggregated sensor data from the sensor_data_30m bucket.
 * Handles downsampled humidity sensor data averaged to 30-minute intervals
 * for long-term trend analysis.
 */
@Service
public class SensorDataAggregatedExportService extends AbstractInfluxExport<SensorDataAggregatedDTO> {

    private static final String BUCKET_NAME = "sensor_data_30m";
    private static final String DEFAULT_AGGREGATION_WINDOW = "30m";

    @Override
    protected String getBucketName() {
        return BUCKET_NAME;
    }

    @Override
    protected String buildQuery(Instant startTime, Instant endTime) {
        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurements(
                    // Aggregated sensor measurements we want to export
                    "sensor_aggregated", "sensor_mean", "sensor_stats"
                )
                .keepOriginalColumns(true)
                .sort("desc") // Most recent first
                .build();
    }

    @Override
    protected Optional<SensorDataAggregatedDTO> convertRecord(FluxRecord record) {
        try {
            // Use the DataTypeHandler to convert the record
            Optional<?> converted = DataTypeHandler.convertRecord(record, BUCKET_NAME);
            if (converted.isPresent() && converted.get() instanceof SensorDataAggregatedDTO dto) {
                // Validate the DTO using DataTypeHandler
                if (DataTypeHandler.validateDTO(dto, BUCKET_NAME)) {
                    return Optional.of(dto);
                } else {
                    logger.debug("SensorDataAggregatedDTO validation failed for record: {}", record);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to convert aggregated sensor data record: {}", record, e);
        }
        return Optional.empty();
    }

    @Override
    protected String getDataTypeDescription() {
        return "Aggregated sensor data (30-minute averaged values for trend analysis)";
    }

    /**
     * Builds a query for aggregated humidity sensor data only.
     */
    public String buildAggregatedHumidityQuery(Instant startTime, Instant endTime) {
        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurements("sensor_aggregated", "sensor_mean")
                .fields(MeasurementMappings.SensorDataAggregatedMappings.HUMIDITY_AGGREGATED_FIELDS.toArray(new String[0]))
                .tag("device_class", "humidity")
                .tag("window", DEFAULT_AGGREGATION_WINDOW)
                .sort("desc")
                .build();
    }

    /**
     * Builds a query for aggregated temperature sensor data only.
     */
    public String buildAggregatedTemperatureQuery(Instant startTime, Instant endTime) {
        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurements("sensor_aggregated", "sensor_mean")
                .fields(MeasurementMappings.SensorDataAggregatedMappings.TEMPERATURE_AGGREGATED_FIELDS.toArray(new String[0]))
                .tagRegex("device_class", "(temperature|thermal)")
                .tag("window", DEFAULT_AGGREGATION_WINDOW)
                .sort("desc")
                .build();
    }

    /**
     * Builds a query for aggregated energy monitoring data only.
     */
    public String buildAggregatedEnergyQuery(Instant startTime, Instant endTime) {
        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurements("sensor_aggregated", "sensor_mean")
                .fields(MeasurementMappings.SensorDataAggregatedMappings.ENERGY_AGGREGATED_FIELDS.toArray(new String[0]))
                .tagRegex("device_class", "(power|energy|current|voltage)")
                .tag("window", DEFAULT_AGGREGATION_WINDOW)
                .sort("desc")
                .build();
    }

    /**
     * Builds a query for statistical aggregation data.
     */
    public String buildStatisticalAggregationQuery(Instant startTime, Instant endTime) {
        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurements("sensor_stats")
                .fields(MeasurementMappings.SensorDataAggregatedMappings.AGGREGATION_FIELDS.toArray(new String[0]))
                .tag("window", DEFAULT_AGGREGATION_WINDOW)
                .sort("desc")
                .build();
    }

    /**
     * Builds a query for a specific aggregation window.
     */
    public String buildWindowQuery(String window, Instant startTime, Instant endTime) {
        if (!MeasurementMappings.SensorDataAggregatedMappings.SUPPORTED_WINDOWS.contains(window)) {
            throw new IllegalArgumentException("Unsupported aggregation window: " + window +
                ". Supported windows: " + MeasurementMappings.SensorDataAggregatedMappings.SUPPORTED_WINDOWS);
        }

        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurements("sensor_aggregated", "sensor_mean", "sensor_stats")
                .tag("window", window)
                .sort("desc")
                .build();
    }

    /**
     * Builds a query for aggregated sensors by location/room.
     */
    public String buildAggregatedLocationQuery(String location, Instant startTime, Instant endTime) {
        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurements("sensor_aggregated", "sensor_mean")
                .tag("location", location)
                .tag("window", DEFAULT_AGGREGATION_WINDOW)
                .sort("desc")
                .build();
    }

    /**
     * Builds a query for aggregated sensors by entity ID.
     */
    public String buildAggregatedEntityQuery(String entityId, Instant startTime, Instant endTime) {
        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurements("sensor_aggregated", "sensor_mean")
                .tag("entity_id", entityId)
                .tag("window", DEFAULT_AGGREGATION_WINDOW)
                .sort("desc")
                .build();
    }

    /**
     * Exports aggregated humidity sensor data specifically.
     */
    public java.util.List<SensorDataAggregatedDTO> exportAggregatedHumidity(Optional<Instant> startTime, Optional<Instant> endTime) {
        Instant actualStartTime = startTime.orElse(getDefaultStartTime());
        Instant actualEndTime = endTime.orElse(getDefaultEndTime());

        String query = buildAggregatedHumidityQuery(actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports aggregated temperature sensor data specifically.
     */
    public java.util.List<SensorDataAggregatedDTO> exportAggregatedTemperature(Optional<Instant> startTime, Optional<Instant> endTime) {
        Instant actualStartTime = startTime.orElse(getDefaultStartTime());
        Instant actualEndTime = endTime.orElse(getDefaultEndTime());

        String query = buildAggregatedTemperatureQuery(actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports aggregated energy monitoring data specifically.
     */
    public java.util.List<SensorDataAggregatedDTO> exportAggregatedEnergy(Optional<Instant> startTime, Optional<Instant> endTime) {
        Instant actualStartTime = startTime.orElse(getDefaultStartTime());
        Instant actualEndTime = endTime.orElse(getDefaultEndTime());

        String query = buildAggregatedEnergyQuery(actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports statistical aggregation data specifically.
     */
    public java.util.List<SensorDataAggregatedDTO> exportStatisticalAggregations(Optional<Instant> startTime, Optional<Instant> endTime) {
        Instant actualStartTime = startTime.orElse(getDefaultStartTime());
        Instant actualEndTime = endTime.orElse(getDefaultEndTime());

        String query = buildStatisticalAggregationQuery(actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports aggregated data for a specific time window.
     */
    public java.util.List<SensorDataAggregatedDTO> exportByAggregationWindow(String window, Optional<Instant> startTime, Optional<Instant> endTime) {
        Instant actualStartTime = startTime.orElse(getDefaultStartTime());
        Instant actualEndTime = endTime.orElse(getDefaultEndTime());

        String query = buildWindowQuery(window, actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports aggregated sensor data for a specific location/room.
     */
    public java.util.List<SensorDataAggregatedDTO> exportAggregatedByLocation(String location, Optional<Instant> startTime, Optional<Instant> endTime) {
        Instant actualStartTime = startTime.orElse(getDefaultStartTime());
        Instant actualEndTime = endTime.orElse(getDefaultEndTime());

        String query = buildAggregatedLocationQuery(location, actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports aggregated sensor data for a specific entity.
     */
    public java.util.List<SensorDataAggregatedDTO> exportAggregatedByEntity(String entityId, Optional<Instant> startTime, Optional<Instant> endTime) {
        Instant actualStartTime = startTime.orElse(getDefaultStartTime());
        Instant actualEndTime = endTime.orElse(getDefaultEndTime());

        String query = buildAggregatedEntityQuery(entityId, actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports aggregated data for the last 24 hours with 30-minute windows.
     */
    public java.util.List<SensorDataAggregatedDTO> exportLast24Hours() {
        Instant endTime = Instant.now();
        Instant startTime = endTime.minusSeconds(24 * 60 * 60); // 24 hours ago

        return exportData(Optional.of(startTime), Optional.of(endTime));
    }

    /**
     * Exports aggregated data for the last 7 days with 30-minute windows.
     */
    public java.util.List<SensorDataAggregatedDTO> exportLast7Days() {
        Instant endTime = Instant.now();
        Instant startTime = endTime.minusSeconds(7 * 24 * 60 * 60); // 7 days ago

        String query = InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurements("sensor_aggregated", "sensor_mean")
                .tag("window", DEFAULT_AGGREGATION_WINDOW)
                .sort("desc")
                .build();

        return executeQueryAndConvert(query);
    }

    /**
     * Helper method to execute a query and convert results.
     */
    private java.util.List<SensorDataAggregatedDTO> executeQueryAndConvert(String query) {
        try {
            return executeQuery(query).stream()
                    .flatMap(table -> table.getRecords().stream())
                    .map(this::convertRecord)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();
        } catch (Exception e) {
            logger.error("Failed to execute aggregated sensor data query: {}", query, e);
            throw new InfluxExportException("Failed to execute aggregated sensor data query", e);
        }
    }
}