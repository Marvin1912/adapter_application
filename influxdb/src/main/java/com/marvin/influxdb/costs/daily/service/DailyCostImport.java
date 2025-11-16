package com.marvin.influxdb.costs.daily.service;

import com.influxdb.client.InfluxDBClient;
import com.marvin.common.costs.DailyCostDTO;
import com.marvin.influxdb.costs.AbstractCostImport;
import com.marvin.influxdb.costs.CostType;
import com.marvin.influxdb.costs.daily.dto.DailyCostMeasurement;
import org.springframework.stereotype.Component;

/**
 * Service class for importing daily cost data into InfluxDB.
 *
 * <p>This component extends the AbstractCostImport to provide specific functionality
 * for importing daily cost measurements. It handles the mapping from DailyCostDTO to DailyCostMeasurement for storage in InfluxDB.</p>
 *
 * @author Marvin Application
 * @version 1.0
 * @since 1.0
 */
@Component
public class DailyCostImport extends AbstractCostImport<DailyCostDTO, DailyCostMeasurement> {

    /**
     * Constructs a DailyCostImport with the specified InfluxDB client.
     *
     * @param influxDBClient the InfluxDB client to use for writing daily cost data
     */
    public DailyCostImport(final InfluxDBClient influxDBClient) {
        super(influxDBClient);
    }

    /**
     * Maps a DailyCostDTO to a DailyCostMeasurement.
     *
     * <p>This method transforms the daily cost DTO into a measurement object
     * that can be written to InfluxDB, setting the appropriate cost type tag and converting the date to an instant.</p>
     *
     * @param dailyCost the daily cost DTO to map
     * @return the corresponding daily cost measurement
     */
    @Override
    protected DailyCostMeasurement map(final DailyCostDTO dailyCost) {
        return new DailyCostMeasurement(
                CostType.DAILY.getValue(),
                dailyCost.value(),
                getAsInstant(dailyCost.costDate())
        );
    }
}
