package com.marvin.influxdb.costs.salary.dto;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import java.math.BigDecimal;
import java.time.Instant;

@Measurement(name = "costs")
public record SalaryMeasurement(
    @Column(tag = true) String costType,
    @Column BigDecimal value,
    @Column(timestamp = true) Instant time
) {

}
