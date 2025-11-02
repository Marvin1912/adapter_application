package com.marvin.influxdb.costs.monthly.service;

import com.influxdb.client.InfluxDBClient;
import com.marvin.common.costs.MonthlyCostDTO;
import com.marvin.influxdb.costs.AbstractCostImport;
import com.marvin.influxdb.costs.CostType;
import com.marvin.influxdb.costs.monthly.dto.MonthlyCostMeasurement;
import org.springframework.stereotype.Component;

@Component
public class MonthlyCostImport extends AbstractCostImport<MonthlyCostDTO, MonthlyCostMeasurement> {

  public MonthlyCostImport(InfluxDBClient influxDBClient) {
    super(influxDBClient);
  }

  @Override
  protected MonthlyCostMeasurement map(MonthlyCostDTO monthlyCost) {
    return new MonthlyCostMeasurement(CostType.MONTHLY.getValue(), monthlyCost.value(),
        getAsInstant(monthlyCost.costDate()));
  }
}
