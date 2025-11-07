package com.marvin.influxdb.costs.monthly.dto;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * InfluxDB measurement record for monthly cost data.
 *
 * <p>This record represents a monthly cost measurement that can be stored in InfluxDB.
 * It includes the cost type as a tag, the monetary value, and the timestamp of the measurement.</p>
 *
 * @param costType the cost type tag used for categorizing the measurement
 * @param value the monetary value of the monthly cost
 * @param time the timestamp when the cost measurement was recorded
 * @author Marvin Application
 * @version 1.0
 * @since 1.0
 */
@Measurement(name = "costs")
public record MonthlyCostMeasurement(
        @Column(tag = true) String costType,
        @Column BigDecimal value,
        @Column(timestamp = true) Instant time
) {
}
