package com.marvin.influxdb.costs.salary.dto;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * InfluxDB measurement record for salary cost data.
 *
 * <p>This record represents a salary cost measurement that can be stored in InfluxDB.
 * It includes the cost type as a tag, the monetary value, and the timestamp of the measurement.</p>
 *
 * @param costType the cost type tag used for categorizing the measurement
 * @param value the monetary value of the salary cost
 * @param time the timestamp when the cost measurement was recorded
 * @author Marvin Application
 * @version 1.0
 * @since 1.0
 */
@Measurement(name = "costs")
public record SalaryMeasurement(

        /** The cost type tag used for categorizing the measurement. */
        @Column(tag = true)
        String costType,

        /** The monetary value of the salary cost. */
        @Column
        BigDecimal value,

        /** The timestamp when the cost measurement was recorded. */
        @Column(timestamp = true)
        Instant time
) {
}
