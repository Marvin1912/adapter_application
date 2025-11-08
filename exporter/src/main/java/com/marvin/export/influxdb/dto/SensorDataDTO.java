package com.marvin.export.influxdb.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

/**
 * Data Transfer Object for IoT sensor data from the sensor_data bucket.
 * Contains humidity sensors, energy monitoring, and other IoT sensor readings
 * from Home Assistant integration.
 */
public record SensorDataDTO(
    String measurement,
    String entityId,
    String friendlyName,
    String deviceClass,
    String unitOfMeasurement,
    Instant timestamp,
    Map<String, Object> fields,
    Map<String, String> tags
) {
    // Common sensor value getters
    public BigDecimal getValue() {
        Object value = fields.get("value");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    // Humidity sensor specific getters
    public BigDecimal getHumidity() {
        Object value = fields.get("humidity");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getTemperature() {
        Object value = fields.get("temperature");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    // Energy monitoring specific getters
    public BigDecimal getCurrent() {
        Object value = fields.get("current");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getVoltage() {
        Object value = fields.get("voltage");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getPower() {
        Object value = fields.get("power");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getEnergy() {
        Object value = fields.get("energy");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getApparentPower() {
        Object value = fields.get("apparent_power");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getPowerFactor() {
        Object value = fields.get("power_factor");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    // Common tag getters
    public String getSource() {
        return tags != null ? tags.get("source") : null;
    }

    public String getDevice() {
        return tags != null ? tags.get("device") : null;
    }

    public String getSensorType() {
        return tags != null ? tags.get("sensor_type") : null;
    }

    public String getLocation() {
        return tags != null ? tags.get("location") : null;
    }

    public String getRoom() {
        return tags != null ? tags.get("room") : null;
    }

    // Helper methods to check sensor type
    public boolean isHumiditySensor() {
        return "humidity".equals(deviceClass) || "%".equals(unitOfMeasurement);
    }

    public boolean isEnergyMonitor() {
        return "power".equals(deviceClass) ||
               "A".equals(unitOfMeasurement) ||
               "V".equals(unitOfMeasurement) ||
               "W".equals(unitOfMeasurement) ||
               "kWh".equals(unitOfMeasurement) ||
               "VA".equals(unitOfMeasurement);
    }

    public boolean isTemperatureSensor() {
        return "temperature".equals(deviceClass) ||
               unitOfMeasurement != null && unitOfMeasurement.contains("°");
    }

    /**
     * Determines if this is a Xiaomi Aqara sensor based on naming patterns.
     */
    public boolean isXiaomiSensor() {
        return friendlyName != null &&
               (friendlyName.contains("Aqara") ||
                friendlyName.contains("Xiaomi") ||
                entityId != null && entityId.contains("xiaomi"));
    }

    /**
     * Determines if this is a Tasmota energy monitor based on naming patterns.
     */
    public boolean isTasmotaDevice() {
        return friendlyName != null &&
               (friendlyName.contains("Tasmota") ||
                entityId != null && entityId.contains("tasmota"));
    }
}