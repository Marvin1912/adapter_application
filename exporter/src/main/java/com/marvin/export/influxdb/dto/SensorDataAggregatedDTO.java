package com.marvin.export.influxdb.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

/**
 * Data Transfer Object for aggregated sensor data from the sensor_data_30m bucket.
 * Contains downsampled humidity sensor data averaged to 30-minute intervals
 * for long-term trend analysis.
 *
 * @param measurement the InfluxDB measurement name
 * @param entityId the unique identifier for the sensor entity
 * @param friendlyName the human-readable name of the sensor
 * @param deviceClass the class/type of the sensor device
 * @param unitOfMeasurement the unit in which the sensor value is measured
 * @param windowStart the start time of the aggregation window
 * @param windowEnd the end time of the aggregation window
 * @param fields map of field names to their aggregated values
 * @param tags map of tag names to their values
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
        final Object value = fields.get("mean");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getAverage() {
        final Object value = fields.get("average");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getMin() {
        final Object value = fields.get("min");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getMax() {
        final Object value = fields.get("max");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getSum() {
        final Object value = fields.get("sum");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public Long getCount() {
        final Object value = fields.get("count");
        return value != null ? Long.valueOf(value.toString()) : null;
    }

    public BigDecimal getStandardDeviation() {
        final Object value = fields.get("stddev");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    // Specific aggregated humidity sensor getters
    public BigDecimal getHumidityMean() {
        final Object value = fields.get("humidity_mean");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getHumidityMin() {
        final Object value = fields.get("humidity_min");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getHumidityMax() {
        final Object value = fields.get("humidity_max");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    // Specific aggregated temperature sensor getters
    public BigDecimal getTemperatureMean() {
        final Object value = fields.get("temperature_mean");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getTemperatureMin() {
        final Object value = fields.get("temperature_min");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getTemperatureMax() {
        final Object value = fields.get("temperature_max");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    // Specific aggregated energy monitoring getters
    public BigDecimal getPowerMean() {
        final Object value = fields.get("power_mean");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getEnergySum() {
        final Object value = fields.get("energy_sum");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getCurrentMean() {
        final Object value = fields.get("current_mean");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getVoltageMean() {
        final Object value = fields.get("voltage_mean");
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