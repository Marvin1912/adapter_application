package com.marvin.export.influxdb.services;

import com.influxdb.query.FluxRecord;
import com.marvin.export.influxdb.AbstractInfluxExport;
import com.marvin.export.influxdb.InfluxQueryBuilder;
import com.marvin.export.influxdb.dto.SensorDataDTO;
import com.marvin.export.influxdb.handlers.DataTypeHandler;
import com.marvin.export.influxdb.mappings.MeasurementMappings;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

/**
 * Export service for humidity sensor data from the sensor_data bucket.
 * Handles humidity readings from various sensors including Xiaomi Aqara weather sensors.
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
        return buildHumiditySensorQuery(startTime, endTime);
    }

    @Override
    protected Optional<SensorDataDTO> convertRecord(FluxRecord record) {
        try {
            // Use the DataTypeHandler to convert the record
            final Object converted = DataTypeHandler.convertRecord(record, BUCKET_NAME);
            if (converted instanceof SensorDataDTO dto) {
                // Filter to include only humidity records
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

    @Override
    protected String getDataTypeDescription() {
        return "Humidity sensor data from IoT devices";
    }

    /**
     * Builds a query for humidity sensor data only.
     *
     * @param startTime the start time for the query
     * @param endTime the end time for the query
     * @return the Flux query string for humidity sensors
     */
    public String buildHumiditySensorQuery(Instant startTime, Instant endTime) {
        return InfluxQueryBuilder.from(BUCKET_NAME)
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
                .tagRegex("device_class", "humidity")
                .keepOriginalColumns(false)
                .sort("desc")
                .build();
    }

    /**
     * Exports humidity sensor data specifically.
     *
     * @param startTime the optional start time for export
     * @param endTime the optional end time for export
     * @return list of humidity sensor data objects
     */
    public java.util.List<SensorDataDTO> exportHumiditySensors(Instant startTime, Instant endTime) {
        final Instant actualStartTime = startTime != null ? startTime : getDefaultStartTime();
        final Instant actualEndTime = endTime != null ? endTime : getDefaultEndTime();

        final String query = buildHumiditySensorQuery(actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Main export method that returns humidity data for streaming.
     *
     * @param startTime the optional start time for export
     * @param endTime the optional end time for export
     * @return stream of humidity sensor data objects
     */
    public java.util.stream.Stream<SensorDataDTO> exportHumidityData(Instant startTime, Instant endTime) {
        return exportHumiditySensors(startTime, endTime).stream();
    }

    /**
     * Helper method to execute a query and convert results.
     *
     * @param query the Flux query to execute
     * @return list of converted humidity sensor data DTOs
     */
    private java.util.List<SensorDataDTO> executeQueryAndConvert(String query) {
        try {
            return executeQuery(query).stream()
                    .flatMap(table -> table.getRecords().stream())
                    .map(this::convertRecord)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();
        } catch (Exception e) {
            LOGGER.error("Failed to execute humidity sensor data query: {}", query, e);
            throw new RuntimeException("Failed to execute humidity sensor data query", e);
        }
    }
}