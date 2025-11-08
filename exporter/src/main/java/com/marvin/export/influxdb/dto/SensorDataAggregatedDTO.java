package com.marvin.export.influxdb.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

/**
 * Data Transfer Object for aggregated sensor data from the sensor_data_30m bucket.
 * Contains downsampled humidity sensor data averaged to 30-minute intervals
 * for long-term trend analysis.
 */
public record SensorDataAggregatedDTO(
    String measurement,
    String entityId,
    String friendlyName,
    String deviceClass,
    String unitOfMeasurement,
    Instant windowStart,
    Instant windowEnd,
    Map<String, Object> fields,
    Map<String, String> tags
) {
    // Aggregated value getters
    public BigDecimal getMean() {
        Object value = fields.get("mean");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getAverage() {
        Object value = fields.get("average");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getMin() {
        Object value = fields.get("min");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getMax() {
        Object value = fields.get("max");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getSum() {
        Object value = fields.get("sum");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public Long getCount() {
        Object value = fields.get("count");
        return value != null ? Long.valueOf(value.toString()) : null;
    }

    public BigDecimal getStandardDeviation() {
        Object value = fields.get("stddev");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    // Specific aggregated humidity sensor getters
    public BigDecimal getHumidityMean() {
        Object value = fields.get("humidity_mean");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getHumidityMin() {
        Object value = fields.get("humidity_min");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getHumidityMax() {
        Object value = fields.get("humidity_max");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    // Specific aggregated temperature sensor getters
    public BigDecimal getTemperatureMean() {
        Object value = fields.get("temperature_mean");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getTemperatureMin() {
        Object value = fields.get("temperature_min");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getTemperatureMax() {
        Object value = fields.get("temperature_max");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    // Specific aggregated energy monitoring getters
    public BigDecimal getPowerMean() {
        Object value = fields.get("power_mean");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getEnergySum() {
        Object value = fields.get("energy_sum");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getCurrentMean() {
        Object value = fields.get("current_mean");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getVoltageMean() {
        Object value = fields.get("voltage_mean");
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

    public String getAggregationWindow() {
        return tags != null ? tags.get("window") : null;
    }

    /**
     * Returns the center timestamp of the aggregation window.
     */
    public Instant getCenterTimestamp() {
        if (windowStart != null && windowEnd != null) {
            return windowStart.plusSeconds(windowEnd.getEpochSecond() - windowStart.getEpochSecond() / 2);
        }
        return windowStart;
    }

    /**
     * Returns the duration of the aggregation window in minutes.
     */
    public Long getWindowDurationMinutes() {
        if (windowStart != null && windowEnd != null) {
            return (windowEnd.getEpochSecond() - windowStart.getEpochSecond()) / 60;
        }
        return null;
    }

    /**
     * Determines if this is aggregated humidity data.
     */
    public boolean isHumidityData() {
        return getHumidityMean() != null ||
               "humidity".equals(deviceClass) ||
               "%".equals(unitOfMeasurement);
    }

    /**
     * Determines if this is aggregated temperature data.
     */
    public boolean isTemperatureData() {
        return getTemperatureMean() != null ||
               "temperature".equals(deviceClass) ||
               unitOfMeasurement != null && unitOfMeasurement.contains("°");
    }

    /**
     * Determines if this is aggregated energy data.
     */
    public boolean isEnergyData() {
        return getPowerMean() != null ||
               getEnergySum() != null ||
               "power".equals(deviceClass) ||
               "W".equals(unitOfMeasurement) ||
               "kWh".equals(unitOfMeasurement);
    }
}