package com.marvin.export.influxdb.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

/**
 * Data Transfer Object for IoT sensor data from the sensor_data bucket. Contains humidity sensors, energy monitoring, and other IoT sensor readings from Home
 * Assistant integration.
 *
 * @param measurement  the InfluxDB measurement name
 * @param entityId     the unique identifier for the sensor entity
 * @param friendlyName the human-readable name of the sensor
 * @param timestamp    the exact timestamp when the sensor reading was recorded
 * @param field        field name to their value
 * @param tags         map of tag names to their values
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SensorDataDTO(
        String measurement,
        String entityId,
        String friendlyName,
        Long timestamp,
        Object field,
        Map<String, String> tags
) {

    public boolean isHumiditySensor() {
        return "%".equals(measurement);
    }

    public boolean isTemperatureSensor() {
        return "°C".equals(measurement);
    }
}
