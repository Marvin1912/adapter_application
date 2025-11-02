package com.marvin.influxdb.costs.salary.service;

import com.influxdb.client.InfluxDBClient;
import com.marvin.common.costs.SalaryDTO;
import com.marvin.influxdb.costs.AbstractCostImport;
import com.marvin.influxdb.costs.CostType;
import com.marvin.influxdb.costs.salary.dto.SalaryMeasurement;
import org.springframework.stereotype.Component;

@Component
public class SalaryImport extends AbstractCostImport<SalaryDTO, SalaryMeasurement> {

    public SalaryImport(InfluxDBClient influxDBClient) {
        super(influxDBClient);
    }

    @Override
    protected SalaryMeasurement map(SalaryDTO salary) {
        return new SalaryMeasurement(CostType.SALARY.getValue(), salary.value(),
                getAsInstant(salary.salaryDate()));
    }
}
