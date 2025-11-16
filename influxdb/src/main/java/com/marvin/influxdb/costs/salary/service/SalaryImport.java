package com.marvin.influxdb.costs.salary.service;

import com.influxdb.client.InfluxDBClient;
import com.marvin.common.costs.SalaryDTO;
import com.marvin.influxdb.costs.AbstractCostImport;
import com.marvin.influxdb.costs.CostType;
import com.marvin.influxdb.costs.salary.dto.SalaryMeasurement;
import org.springframework.stereotype.Component;

/**
 * Service class for importing salary cost data into InfluxDB.
 *
 * <p>This component extends the AbstractCostImport to provide specific functionality
 * for importing salary cost measurements. It handles the mapping from SalaryDTO to SalaryMeasurement for storage in InfluxDB.</p>
 *
 * @author Marvin Application
 * @version 1.0
 * @since 1.0
 */
@Component
public class SalaryImport extends AbstractCostImport<SalaryDTO, SalaryMeasurement> {

    /**
     * Constructs a SalaryImport with the specified InfluxDB client.
     *
     * @param influxDBClient the InfluxDB client to use for writing salary cost data
     */
    public SalaryImport(final InfluxDBClient influxDBClient) {
        super(influxDBClient);
    }

    /**
     * Maps a SalaryDTO to a SalaryMeasurement.
     *
     * <p>This method transforms the salary DTO into a measurement object
     * that can be written to InfluxDB, setting the appropriate cost type tag and converting the date to an instant.</p>
     *
     * @param salary the salary DTO to map
     * @return the corresponding salary cost measurement
     */
    @Override
    protected SalaryMeasurement map(final SalaryDTO salary) {
        return new SalaryMeasurement(
                CostType.SALARY.getValue(),
                salary.value(),
                getAsInstant(salary.salaryDate())
        );
    }
}
