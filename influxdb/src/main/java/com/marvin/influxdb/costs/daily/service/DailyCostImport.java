package com.marvin.influxdb.costs.daily.service;

import com.influxdb.client.InfluxDBClient;
import com.marvin.common.costs.DailyCostDTO;
import com.marvin.influxdb.costs.AbstractCostImport;
import com.marvin.influxdb.costs.CostType;
import com.marvin.influxdb.costs.daily.dto.DailyCostMeasurement;
import org.springframework.stereotype.Component;

@Component
public class DailyCostImport extends AbstractCostImport<DailyCostDTO, DailyCostMeasurement> {

    public DailyCostImport(InfluxDBClient influxDBClient) {
        super(influxDBClient);
    }

    @Override
    protected DailyCostMeasurement map(DailyCostDTO monthlyCost) {
        return new DailyCostMeasurement(CostType.DAILY.getValue(), monthlyCost.value(),
                getAsInstant(monthlyCost.costDate()));
    }
}
