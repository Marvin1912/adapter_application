package com.marvin.export.influxdb.services;

import com.influxdb.query.FluxRecord;
import com.marvin.export.influxdb.AbstractInfluxExport;
import com.marvin.export.influxdb.InfluxQueryBuilder;
import com.marvin.export.influxdb.dto.SystemMetricsDTO;
import com.marvin.export.influxdb.handlers.DataTypeHandler;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Export service for system metrics data from the system_metrics bucket. Handles CPU, memory, disk, network, and system performance metrics from host
 * home-server.
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
            final Object converted = DataTypeHandler.convertRecord(record, BUCKET_NAME);
            if (converted != null && converted instanceof SystemMetricsDTO dto) {
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

}
