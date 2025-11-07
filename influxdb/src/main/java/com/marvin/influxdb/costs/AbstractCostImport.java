package com.marvin.influxdb.costs;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for importing cost data into InfluxDB.
 *
 * <p>This class provides common functionality for importing different types of cost data
 * into InfluxDB measurements. It handles the conversion between DTOs and measurements,
 * and manages the writing of data to the InfluxDB database.</p>
 *
 * @param <DTO> the type of data transfer object to be imported
 * @param <MEAS> the type of measurement to be written to InfluxDB
 * @author Marvin Application
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractCostImport<DTO, MEAS> {

    /** Logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCostImport.class);

    /** UTC timezone constant for date conversions. */
    private static final String UTC_TIMEZONE = "UTC";

    /** InfluxDB bucket name for costs. */
    private static final String COSTS_BUCKET = "costs";

    /** InfluxDB organization name. */
    private static final String WILDFLY_DOMAIN_ORG = "wildfly_domain";

    /** The InfluxDB client instance. */
    private final InfluxDBClient influxDBClient;

    /**
     * Constructs an AbstractCostImport with the specified InfluxDB client.
     *
     * @param influxDBClient the InfluxDB client to use for writing data
     */
    protected AbstractCostImport(final InfluxDBClient influxDBClient) {
        this.influxDBClient = influxDBClient;
    }

    /**
     * Maps a DTO to its corresponding measurement type.
     *
     * <p>This method must be implemented by subclasses to define the specific
     * mapping logic for each cost type.</p>
     *
     * @param dto the data transfer object to map
     * @return the corresponding measurement object
     */
    protected abstract MEAS map(final DTO dto);

    /**
     * Converts a LocalDate to an Instant in UTC timezone.
     *
     * @param localDate the local date to convert
     * @return the corresponding instant in UTC
     */
    protected Instant getAsInstant(final LocalDate localDate) {
        return localDate.atStartOfDay(ZoneId.of(UTC_TIMEZONE)).toInstant();
    }

    /**
     * Imports cost data from a DTO into InfluxDB.
     *
     * <p>This method maps the DTO to a measurement and writes it to the
     * InfluxDB database using the costs bucket and wildfly_domain organization.</p>
     *
     * @param dto the data transfer object containing cost data to import
     */
    public void importCost(final DTO dto) {
        final MEAS measurement = map(dto);

        final WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
        writeApi.writeMeasurement(COSTS_BUCKET, WILDFLY_DOMAIN_ORG, WritePrecision.NS, measurement);

        LOGGER.info("Successfully imported InfluxDB measurement: {}", measurement);
    }
}
