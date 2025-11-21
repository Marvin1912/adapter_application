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

    public Double getPrimaryValue() {
        if (fields == null || fields.isEmpty()) {
            return null;
        }

        Object value = fields.get("value");
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        return fields.values().stream()
                .filter(Number.class::isInstance)
                .map(Number.class::cast)
                .findFirst()
                .map(Number::doubleValue)
                .orElse(null);
    }
}