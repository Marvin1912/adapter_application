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
 * Handles humidity sensors (%) from Home Assistant integration.
 */
@Service
public class SensorDataExportService extends AbstractInfluxExport<SensorDataDTO> {

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
                .tag("device_class", "humidity")
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
                .sort("desc") // Most recent first
                .build();
    }

    @Override
    protected Optional<SensorDataDTO> convertRecord(FluxRecord record) {
        try {
            // Use the DataTypeHandler to convert the record
            final Object converted = DataTypeHandler.convertRecord(record, BUCKET_NAME);
            if (converted instanceof SensorDataDTO dto) {
                // Validate the DTO using DataTypeHandler
                if (DataTypeHandler.validateDTO(dto, BUCKET_NAME)) {
                    return Optional.of(dto);
                } else {
                    LOGGER.debug("SensorDataDTO validation failed for record: {}", record);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to convert sensor data record: {}", record, e);
        }
        return Optional.empty();
    }

    @Override
    protected String getDataTypeDescription() {
        return "Humidity sensor data (%) from Home Assistant";
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
                .tag("device_class", "humidity")
                .tag("unit_of_measurement", "%")
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

    /**
     * Builds a query for energy monitoring data only.
     *
     * @param startTime the start time for the query
     * @param endTime the end time for the query
     * @return the Flux query string for energy monitoring
     */
    public String buildEnergyMonitoringQuery(Instant startTime, Instant endTime) {
        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurements("sensor", "energy", "power")
                .fields(MeasurementMappings.SensorDataMappings.ENERGY_FIELDS.toArray(new String[0]))
                .tagRegex("device_class", "(power|energy|current|voltage)")
                .sort("desc")
                .build();
    }

    
    /**
     * Builds a query for Xiaomi Aqara sensors specifically.
     *
     * @param startTime the start time for the query
     * @param endTime the end time for the query
     * @return the Flux query string for Xiaomi sensors
     */
    public String buildXiaomiSensorQuery(Instant startTime, Instant endTime) {
        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurements("sensor", "binary_sensor")
                .tagRegex("entity_id", ".*xiaomi.*")
                .tagRegex("friendly_name", ".*(Aqara|Xiaomi).*")
                .sort("desc")
                .build();
    }

    /**
     * Builds a query for Tasmota devices specifically.
     *
     * @param startTime the start time for the query
     * @param endTime the end time for the query
     * @return the Flux query string for Tasmota devices
     */
    public String buildTasmotaDeviceQuery(Instant startTime, Instant endTime) {
        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurements("sensor", "energy", "power")
                .tagRegex("entity_id", ".*tasmota.*")
                .tagRegex("friendly_name", ".*Tasmota.*")
                .sort("desc")
                .build();
    }

    /**
     * Builds a query for sensors by location/room.
     *
     * @param location the location/room to filter sensors
     * @param startTime the start time for the query
     * @param endTime the end time for the query
     * @return the Flux query string for sensors by location
     */
    public String buildLocationSensorQuery(String location, Instant startTime, Instant endTime) {
        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurements("sensor", "binary_sensor")
                .tag("location", location)
                .sort("desc")
                .build();
    }

    /**
     * Builds a query for sensors by device class.
     *
     * @param deviceClass the device class to filter sensors
     * @param startTime the start time for the query
     * @param endTime the end time for the query
     * @return the Flux query string for sensors by device class
     */
    public String buildDeviceClassQuery(String deviceClass, Instant startTime, Instant endTime) {
        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurements("sensor", "binary_sensor", "climate")
                .tag("device_class", deviceClass)
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
     * Exports energy monitoring data specifically.
     *
     * @param startTime the optional start time for export
     * @param endTime the optional end time for export
     * @return list of energy monitoring data objects
     */
    public java.util.List<SensorDataDTO> exportEnergyMonitoring(
            Instant startTime, Instant endTime) {
        final Instant actualStartTime = startTime != null ? startTime : getDefaultStartTime();
        final Instant actualEndTime = endTime != null ? endTime : getDefaultEndTime();

        final String query = buildEnergyMonitoringQuery(actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    
    /**
     * Exports Xiaomi Aqara sensor data specifically.
     *
     * @param startTime the optional start time for export
     * @param endTime the optional end time for export
     * @return list of Xiaomi sensor data objects
     */
    public java.util.List<SensorDataDTO> exportXiaomiSensors(Instant startTime, Instant endTime) {
        final Instant actualStartTime = startTime != null ? startTime : getDefaultStartTime();
        final Instant actualEndTime = endTime != null ? endTime : getDefaultEndTime();

        final String query = buildXiaomiSensorQuery(actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports Tasmota device data specifically.
     *
     * @param startTime the optional start time for export
     * @param endTime the optional end time for export
     * @return list of Tasmota device data objects
     */
    public java.util.List<SensorDataDTO> exportTasmotaDevices(Instant startTime, Instant endTime) {
        final Instant actualStartTime = startTime != null ? startTime : getDefaultStartTime();
        final Instant actualEndTime = endTime != null ? endTime : getDefaultEndTime();

        final String query = buildTasmotaDeviceQuery(actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports sensor data for a specific location/room.
     *
     * @param location the location/room to filter sensors
     * @param startTime the optional start time for export
     * @param endTime the optional end time for export
     * @return list of sensor data objects for the specified location
     */
    public java.util.List<SensorDataDTO> exportByLocation(
            String location, Instant startTime, Instant endTime) {
        final Instant actualStartTime = startTime != null ? startTime : getDefaultStartTime();
        final Instant actualEndTime = endTime != null ? endTime : getDefaultEndTime();

        final String query = buildLocationSensorQuery(location, actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports sensor data for a specific device class.
     *
     * @param deviceClass the device class to filter sensors
     * @param startTime the optional start time for export
     * @param endTime the optional end time for export
     * @return list of sensor data objects for the specified device class
     */
    public java.util.List<SensorDataDTO> exportByDeviceClass(
            String deviceClass, Instant startTime, Instant endTime) {
        final Instant actualStartTime = startTime != null ? startTime : getDefaultStartTime();
        final Instant actualEndTime = endTime != null ? endTime : getDefaultEndTime();

        final String query = buildDeviceClassQuery(deviceClass, actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Helper method to execute a query and convert results.
     *
     * @param query the Flux query to execute
     * @return list of converted sensor data objects
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
            LOGGER.error("Failed to execute sensor data query: {}", query, e);
            throw new InfluxExportException("Failed to execute sensor data query", e);
        }
    }
}