package com.marvin.app.sensor.dto;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.time.Instant;
import java.util.Map;

/**
 * InfluxDB measurement record for sensor data.
 *
 * <p>This record represents sensor data that can be stored in InfluxDB.
 * It supports dynamic measurements, entities, fields, and tags as shown in the JSON example.</p>
 *
 * @param measurement   the name of the measurement (can be wildcard %)
 * @param entityId      the unique identifier of the sensor entity
 * @param friendlyName  the human-readable name of the sensor
 * @param timestamp     the timestamp when the sensor reading was taken
 * @param fields        the sensor values as a map (e.g., humidity value)
 * @param tags          the tags associated with the sensor reading
 * @author Marvin Application
 * @version 1.0
 * @since 1.0
 */
@Measurement(name = "%")
public record SensorData(
    @Column(name = "measurement", tag = true)
    @JsonProperty("measurement")
    String measurement,

    @Column(name = "entity_id", tag = true)
    @JsonProperty("entityId")
    String entityId,

    @Column(name = "friendly_name", tag = true)
    @JsonProperty("friendlyName")
    String friendlyName,

    @Column(timestamp = true)
    @JsonProperty("timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    Instant timestamp,

    @Column(name = "value")
    @JsonProperty("fields")
    Map<String, Object> fields,

    @JsonIgnore
    Map<String, String> tags,

    @JsonIgnore
    @JsonProperty("temperatureSensor")
    boolean temperatureSensor,

    @JsonIgnore
    @JsonProperty("humiditySensor")
    boolean humiditySensor
) {

    /**
     * Gets the primary sensor value from the fields map.
     * Assumes the first value in the fields map is the primary sensor reading.
     *
     * @return the primary sensor value, or null if no fields are present
     */
    public Double getPrimaryValue() {
        if (fields == null || fields.isEmpty()) {
            return null;
        }

        // Try to find a "value" field first
        Object value = fields.get("value");
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        // If no "value" field, return the first numeric value found
        return fields.values().stream()
                .filter(Number.class::isInstance)
                .map(Number.class::cast)
                .findFirst()
                .map(Number::doubleValue)
                .orElse(null);
    }
}