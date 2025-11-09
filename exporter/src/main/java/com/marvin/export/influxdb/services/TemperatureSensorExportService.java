package com.marvin.export.influxdb.services;

import com.influxdb.query.FluxRecord;
import com.marvin.export.influxdb.AbstractInfluxExport;
import com.marvin.export.influxdb.InfluxQueryBuilder;
import com.marvin.export.influxdb.dto.SensorDataDTO;
import com.marvin.export.influxdb.handlers.DataTypeHandler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

/**
 * Export service for temperature sensor data from the sensor_data bucket.
 * Handles temperature readings (°C) from Home Assistant integration.
 */
@Service
public class TemperatureSensorExportService extends AbstractInfluxExport<SensorDataDTO> {

    private static final String BUCKET_NAME = "sensor_data";

    @Override
    protected String getBucketName() {
        return BUCKET_NAME;
    }

    @Override
    protected String buildQuery(Instant startTime, Instant endTime) {
        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurement("%")
                .field("value")
                .tag("device_class", "temperature")
                .map("""
                    fn: (r) => ({
                          r with friendly_name:
                            if r.entity_id == "temperature_sensor_1" then "Wohnzimmer"
                            else if r.entity_id == "temperature_sensor_2" then "Schlafzimmer"
                            else if r.entity_id == "temperature_sensor_3" then "Küche"
                            else if r.entity_id == "temperature_sensor_4" then "Badezimmer"
                            else if r.entity_id == "temperature_sensor_5" then "Flur"
                            else "Nicht bekannt"
                        })""")
                .sort("desc") // Most recent first
                .build();
    }

    @Override
    protected Optional<SensorDataDTO> convertRecord(FluxRecord record) {
        try {
            // Use the DataTypeHandler to convert the record
            final Object converted = DataTypeHandler.convertRecord(record, BUCKET_NAME);
            if (converted != null && converted instanceof SensorDataDTO dto) {
                // Validate the DTO using DataTypeHandler
                if (DataTypeHandler.validateDTO(dto, BUCKET_NAME)) {
                    return Optional.of(dto);
                } else {
                    LOGGER.debug("SensorDataDTO validation failed for record: {}", record);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to convert temperature sensor data record: {}", record, e);
        }
        return Optional.empty();
    }

    @Override
    protected String getDataTypeDescription() {
        return "Temperature sensor data (°C from Home Assistant)";
    }

    /**
     * Builds a query for temperature sensor data with specific filtering.
     *
     * @param startTime the start time for the query
     * @param endTime the end time for the query
     * @return the Flux query string for temperature sensors
     */
    public String buildTemperatureSensorQuery(Instant startTime, Instant endTime) {
        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurement("%")
                .fields("temperature", "value")
                .tag("device_class", "temperature")
                .tag("unit_of_measurement", "°C")
                .map("""
                    fn: (r) => ({
                          r with friendly_name:
                            if r.entity_id == "temperature_sensor_1" then "Wohnzimmer"
                            else if r.entity_id == "temperature_sensor_2" then "Schlafzimmer"
                            else if r.entity_id == "temperature_sensor_3" then "Küche"
                            else if r.entity_id == "temperature_sensor_4" then "Badezimmer"
                            else if r.entity_id == "temperature_sensor_5" then "Flur"
                            else "Nicht bekannt"
                        })""")
                .sort("desc")
                .build();
    }

    /**
     * Builds a query for climate device temperature data.
     *
     * @param startTime the start time for the query
     * @param endTime the end time for the query
     * @return the Flux query string for climate temperature data
     */
    public String buildClimateTemperatureQuery(Instant startTime, Instant endTime) {
        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurement("climate")
                .fields("temperature", "current_temperature")
                .tag("device_class", "temperature")
                .sort("desc")
                .build();
    }

    /**
     * Builds a query for temperature sensors by location/room.
     *
     * @param location the location/room to filter sensors
     * @param startTime the start time for the query
     * @param endTime the end time for the query
     * @return the Flux query string for temperature sensors by location
     */
    public String buildLocationTemperatureQuery(String location, Instant startTime, Instant endTime) {
        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurements("sensor", "climate")
                .fields("temperature", "value", "current_temperature")
                .tag("device_class", "temperature")
                .tag("location", location)
                .sort("desc")
                .build();
    }

    /**
     * Exports temperature sensor data specifically.
     *
     * @param startTime the optional start time for export
     * @param endTime the optional end time for export
     * @return list of temperature sensor data objects
     */
    public java.util.List<SensorDataDTO> exportTemperatureSensors(Instant startTime, Instant endTime) {
        final Instant actualStartTime = startTime != null ? startTime : getDefaultStartTime();
        final Instant actualEndTime = endTime != null ? endTime : getDefaultEndTime();

        final String query = buildTemperatureSensorQuery(actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports climate device temperature data specifically.
     *
     * @param startTime the optional start time for export
     * @param endTime the optional end time for export
     * @return list of climate temperature data objects
     */
    public java.util.List<SensorDataDTO> exportClimateTemperature(Instant startTime, Instant endTime) {
        final Instant actualStartTime = startTime != null ? startTime : getDefaultStartTime();
        final Instant actualEndTime = endTime != null ? endTime : getDefaultEndTime();

        final String query = buildClimateTemperatureQuery(actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports temperature sensor data for a specific location/room.
     *
     * @param location the location/room to filter sensors
     * @param startTime the optional start time for export
     * @param endTime the optional end time for export
     * @return list of temperature sensor data objects for the specified location
     */
    public java.util.List<SensorDataDTO> exportTemperatureByLocation(
            String location, Instant startTime, Instant endTime) {
        final Instant actualStartTime = startTime != null ? startTime : getDefaultStartTime();
        final Instant actualEndTime = endTime != null ? endTime : getDefaultEndTime();

        final String query = buildLocationTemperatureQuery(location, actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Helper method to execute a query and convert results.
     *
     * @param query the Flux query to execute
     * @return list of converted temperature sensor data objects
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
            LOGGER.error("Failed to execute temperature sensor data query: {}", query, e);
            throw new InfluxExportException("Failed to execute temperature sensor data query", e);
        }
    }
}