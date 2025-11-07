package com.marvin.influxdb.costs.monthly.service;

import com.influxdb.client.InfluxDBClient;
import com.marvin.common.costs.MonthlyCostDTO;
import com.marvin.influxdb.costs.AbstractCostImport;
import com.marvin.influxdb.costs.CostType;
import com.marvin.influxdb.costs.monthly.dto.MonthlyCostMeasurement;
import org.springframework.stereotype.Component;

/**
 * Service class for importing monthly cost data into InfluxDB.
 *
 * <p>This component extends the AbstractCostImport to provide specific functionality
 * for importing monthly cost measurements. It handles the mapping from MonthlyCostDTO
 * to MonthlyCostMeasurement for storage in InfluxDB.</p>
 *
 * @author Marvin Application
 * @version 1.0
 * @since 1.0
 */
@Component
public class MonthlyCostImport extends AbstractCostImport<MonthlyCostDTO, MonthlyCostMeasurement> {

    /**
     * Constructs a MonthlyCostImport with the specified InfluxDB client.
     *
     * @param influxDBClient the InfluxDB client to use for writing monthly cost data
     */
    public MonthlyCostImport(final InfluxDBClient influxDBClient) {
        super(influxDBClient);
    }

    /**
     * Maps a MonthlyCostDTO to a MonthlyCostMeasurement.
     *
     * <p>This method transforms the monthly cost DTO into a measurement object
     * that can be written to InfluxDB, setting the appropriate cost type tag
     * and converting the date to an instant.</p>
     *
     * @param monthlyCost the monthly cost DTO to map
     * @return the corresponding monthly cost measurement
     */
    @Override
    protected MonthlyCostMeasurement map(final MonthlyCostDTO monthlyCost) {
        return new MonthlyCostMeasurement(
                CostType.MONTHLY.getValue(),
                monthlyCost.value(),
                getAsInstant(monthlyCost.costDate())
        );
    }
}
