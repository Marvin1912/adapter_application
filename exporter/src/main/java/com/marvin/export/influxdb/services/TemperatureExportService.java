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
 * Export service for temperature sensor data from the sensor_data bucket. Handles temperature readings from various sensors including Home Assistant climate
 * devices.
 */
@Service
public class TemperatureExportService extends AbstractInfluxExport<SensorDataDTO> {

    private static final String BUCKET_NAME = "sensor_data";

    @Override
    protected String getBucketName() {
        return BUCKET_NAME;
    }

    @Override
    protected String buildQuery(Instant startTime, Instant endTime) {
        return InfluxQueryBuilder.from(getBucketName())
                .timeRange(startTime, endTime)
                .measurement("°C")
                .field("value")
                .map("""
                        fn: (r) => ({
                              r with friendly_name:
                                if r.entity_id == "lumi_lumi_weather_temperatur" then "Badezimmer"
                                else if r.entity_id == "lumi_lumi_weather_temperatur_2" then "Flur"
                                else if r.entity_id == "lumi_lumi_weather_temperatur_3" then "Küche"
                                else if r.entity_id == "lumi_lumi_weather_temperatur_4" then "Schlafzimmer"
                                else if r.entity_id == "lumi_lumi_weather_temperatur_5" then "Wohnzimmer"
                                else "Nicht bekannt"
                            })""")
                .keepOriginalColumns(false)
                .sort("desc")
                .build();
    }

    @Override
    protected Optional<SensorDataDTO> convertRecord(FluxRecord record) {
        try {
            // Use the DataTypeHandler to convert the record
            final Object converted = DataTypeHandler.convertRecord(record, BUCKET_NAME);
            if (converted instanceof SensorDataDTO dto) {
                // Filter to include only temperature records
                if (dto.isTemperatureSensor() && DataTypeHandler.validateDTO(dto, BUCKET_NAME)) {
                    return Optional.of(dto);
                } else {
                    LOGGER.debug("Non-temperature sensor record filtered out: {}", record);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to convert temperature sensor data record: {}", record, e);
        }
        return Optional.empty();
    }
}
