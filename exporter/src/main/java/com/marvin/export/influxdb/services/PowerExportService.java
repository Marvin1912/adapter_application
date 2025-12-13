package com.marvin.export.influxdb.services;

import com.influxdb.query.FluxRecord;
import com.marvin.export.influxdb.AbstractInfluxExport;
import com.marvin.export.influxdb.InfluxQueryBuilder;
import com.marvin.export.influxdb.dto.SensorDataDTO;
import com.marvin.export.influxdb.handlers.DataTypeHandler;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Export service for humidity sensor data from the sensor_data bucket. Handles humidity readings from various sensors including Xiaomi Aqara weather sensors.
 */
@Service
public class PowerExportService extends AbstractInfluxExport<SensorDataDTO> {

    private static final String BUCKET_NAME = "sensor_data";

    @Override
    protected String getBucketName() {
        return BUCKET_NAME;
    }

    @Override
    protected String buildQuery(Instant startTime, Instant endTime) {
        return InfluxQueryBuilder.from(getBucketName())
            .timeRange(startTime, endTime)
            .measurement("W")
            .field("value")
            .map("""
                fn: (r) => ({
                      r with friendly_name:
                        if r.entity_id == "tasmota_energy_power" then "Kühlschrank"
                        else if r.entity_id == "tasmota_energy_power_2" then "Server"
                        else if r.entity_id == "tasmota_energy_power_3" then "Workstation"
                        else if r.entity_id == "tasmota_energy_power_4" then "Waschmaschine"
                        else "Nicht bekannt"
                    })""")
            .keepOriginalColumns(false)
            .sort("desc")
            .build();
    }

    @Override
    protected Optional<SensorDataDTO> convertRecord(FluxRecord record) {
        try {
            final Object converted = DataTypeHandler.convertRecord(record, BUCKET_NAME);
            if (converted instanceof SensorDataDTO dto) {
                if (dto.isPowerSensor() && DataTypeHandler.validateDTO(dto, BUCKET_NAME)) {
                    return Optional.of(dto);
                } else {
                    LOGGER.debug("Non-power sensor record filtered out: {}", record);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to convert humidity sensor data record: {}", record, e);
        }
        return Optional.empty();
    }
}
