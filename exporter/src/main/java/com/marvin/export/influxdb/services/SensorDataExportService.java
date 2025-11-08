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
 * Export service for IoT sensor data from the sensor_data bucket.
 * Handles humidity sensors, energy monitoring, and other IoT sensor readings
 * from Home Assistant integration.
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
                .measurements(
                    // IoT sensor measurements we want to export
                    "sensor", "binary_sensor", "climate", "energy", "power"
                )
                .keepOriginalColumns(true)
                .sort("desc") // Most recent first
                .build();
    }

    @Override
    protected Optional<SensorDataDTO> convertRecord(FluxRecord record) {
        try {
            // Use the DataTypeHandler to convert the record
            final Optional<?> converted = DataTypeHandler.convertRecord(record, BUCKET_NAME);
            if (converted.isPresent() && converted.get() instanceof SensorDataDTO dto) {
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
        return "IoT sensor data (humidity, energy monitoring from Home Assistant)";
    }

    /**
     * Builds a query for humidity sensor data only.
     */
    public String buildHumiditySensorQuery(Instant startTime, Instant endTime) {
        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurements("sensor", "binary_sensor")
                .fields(MeasurementMappings.SensorDataMappings.HUMIDITY_FIELDS.toArray(new String[0]))
                .tag("device_class", "humidity")
                .sort("desc")
                .build();
    }

    /**
     * Builds a query for energy monitoring data only.
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
     * Builds a query for temperature sensor data only.
     */
    public String buildTemperatureSensorQuery(Instant startTime, Instant endTime) {
        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurements("sensor", "climate")
                .fields("temperature", "value")
                .tagRegex("device_class", "(temperature|thermal)")
                .sort("desc")
                .build();
    }

    /**
     * Builds a query for Xiaomi Aqara sensors specifically.
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
     */
    public java.util.List<SensorDataDTO> exportHumiditySensors(Optional<Instant> startTime, Optional<Instant> endTime) {
        final Instant actualStartTime = startTime.orElse(getDefaultStartTime());
        final Instant actualEndTime = endTime.orElse(getDefaultEndTime());

        final String query = buildHumiditySensorQuery(actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports energy monitoring data specifically.
     */
    public java.util.List<SensorDataDTO> exportEnergyMonitoring(Optional<Instant> startTime, Optional<Instant> endTime) {
        final Instant actualStartTime = startTime.orElse(getDefaultStartTime());
        final Instant actualEndTime = endTime.orElse(getDefaultEndTime());

        final String query = buildEnergyMonitoringQuery(actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports temperature sensor data specifically.
     */
    public java.util.List<SensorDataDTO> exportTemperatureSensors(Optional<Instant> startTime, Optional<Instant> endTime) {
        final Instant actualStartTime = startTime.orElse(getDefaultStartTime());
        final Instant actualEndTime = endTime.orElse(getDefaultEndTime());

        final String query = buildTemperatureSensorQuery(actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports Xiaomi Aqara sensor data specifically.
     */
    public java.util.List<SensorDataDTO> exportXiaomiSensors(Optional<Instant> startTime, Optional<Instant> endTime) {
        final Instant actualStartTime = startTime.orElse(getDefaultStartTime());
        final Instant actualEndTime = endTime.orElse(getDefaultEndTime());

        final String query = buildXiaomiSensorQuery(actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports Tasmota device data specifically.
     */
    public java.util.List<SensorDataDTO> exportTasmotaDevices(Optional<Instant> startTime, Optional<Instant> endTime) {
        final Instant actualStartTime = startTime.orElse(getDefaultStartTime());
        final Instant actualEndTime = endTime.orElse(getDefaultEndTime());

        final String query = buildTasmotaDeviceQuery(actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports sensor data for a specific location/room.
     */
    public java.util.List<SensorDataDTO> exportByLocation(String location, Optional<Instant> startTime, Optional<Instant> endTime) {
        final Instant actualStartTime = startTime.orElse(getDefaultStartTime());
        final Instant actualEndTime = endTime.orElse(getDefaultEndTime());

        final String query = buildLocationSensorQuery(location, actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports sensor data for a specific device class.
     */
    public java.util.List<SensorDataDTO> exportByDeviceClass(String deviceClass, Optional<Instant> startTime, Optional<Instant> endTime) {
        final Instant actualStartTime = startTime.orElse(getDefaultStartTime());
        final Instant actualEndTime = endTime.orElse(getDefaultEndTime());

        final String query = buildDeviceClassQuery(deviceClass, actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Helper method to execute a query and convert results.
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