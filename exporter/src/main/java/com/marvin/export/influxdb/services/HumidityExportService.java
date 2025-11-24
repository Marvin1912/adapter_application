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
public class HumidityExportService extends AbstractInfluxExport<SensorDataDTO> {

    private static final String BUCKET_NAME = "sensor_data";

    @Override
    protected String getBucketName() {
        return BUCKET_NAME;
    }

    @Override
    protected String buildQuery(Instant startTime, Instant endTime) {
        return InfluxQueryBuilder.from(getBucketName())
                .timeRange(startTime, endTime)
                .measurement("%")
                .field("value")
                .map("""
                        fn: (r) => ({
                              r with friendly_name:
                                if r.entity_id == "lumi_lumi_weather_luftfeuchtigkeit" then "Badezimmer"
                                else if r.entity_id == "lumi_lumi_weather_luftfeuchtigkeit_2" then "Flur"
                                else if r.entity_id == "lumi_lumi_weather_luftfeuchtigkeit_3" then "Küche"
                                else if r.entity_id == "lumi_lumi_weather_luftfeuchtigkeit_4" then "Schlafzimmer"
                                else if r.entity_id == "lumi_lumi_weather_luftfeuchtigkeit_5" then "Wohnzimmer"
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
                if (dto.isHumiditySensor() && DataTypeHandler.validateDTO(dto, BUCKET_NAME)) {
                    return Optional.of(dto);
                } else {
                    LOGGER.debug("Non-humidity sensor record filtered out: {}", record);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to convert humidity sensor data record: {}", record, e);
        }
        return Optional.empty();
    }
}
