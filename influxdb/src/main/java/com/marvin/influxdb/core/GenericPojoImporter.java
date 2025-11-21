package com.marvin.influxdb.core;

import com.influxdb.annotations.Measurement;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApiBlocking;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic POJO importer for writing data to InfluxDB.
 *
 * <p>This class provides a generic way to import POJOs into InfluxDB using the
 * influxdb-client-java library. It supports both single POJO and batch operations,
 * and works with any POJO annotated with {@link Measurement}.</p>
 *
 * <p>Example usage:
 * <pre>{@code
 * // Create importer with default configuration
 * GenericPojoImporter<MyPojo> importer = new GenericPojoImporter<>(influxDBClient);
 *
 * // Write a single POJO
 * importer.importPojo(myPojo);
 *
 * // Write multiple POJOs
 * List<MyPojo> pojos = List.of(pojo1, pojo2, pojo3);
 * importer.importBatch(pojos);
 * }</pre></p>
 *
 * @param <T> the type of POJO to be imported
 * @author Marvin Application
 * @version 1.0
 * @since 1.0
 */
@Getter
public class GenericPojoImporter<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericPojoImporter.class);

    private final InfluxDBClient influxDBClient;

    private final InfluxWriteConfig config;

    /**
     * Constructs a GenericPojoImporter with the specified InfluxDB client and default configuration.
     *
     * @param influxDBClient the InfluxDB client to use for writing data
     */
    public GenericPojoImporter(final InfluxDBClient influxDBClient) {
        this(influxDBClient, InfluxWriteConfig.forCostBucket());
    }

    /**
     * Constructs a GenericPojoImporter with the specified InfluxDB client and configuration.
     *
     * @param influxDBClient the InfluxDB client to use for writing data
     * @param config         the write configuration to use
     */
    public GenericPojoImporter(final InfluxDBClient influxDBClient, final InfluxWriteConfig config) {
        this.influxDBClient = influxDBClient;
        this.config = config;
    }

    /**
     * Imports a single POJO into InfluxDB.
     *
     * <p>The POJO must be annotated with {@link Measurement} to specify the measurement name
     * and field mappings using {@link com.influxdb.annotations.Column} annotations.</p>
     *
     * @param pojo the POJO to import
     * @throws IllegalArgumentException if the POJO is not properly annotated
     */
    public void importPojo(final T pojo) {
        validatePojo(pojo);

        final WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
        writeApi.writeMeasurement(config.bucket(), config.organization(), config.writePrecision(), pojo);

        LOGGER.info("Successfully imported POJO of type {} to bucket {}",
                   pojo.getClass().getSimpleName(), config.bucket());
    }

    /**
     * Imports a collection of POJOs into InfluxDB in batch.
     *
     * <p>All POJOs must be annotated with {@link Measurement} and should typically
     * represent the same measurement type for optimal performance. This method converts
     * the collection to a List for the underlying InfluxDB client.</p>
     *
     * @param pojos the collection of POJOs to import
     * @throws IllegalArgumentException if any POJO is not properly annotated or if the collection is empty
     */
    public void importBatch(final Collection<T> pojos) {
        if (pojos == null || pojos.isEmpty()) {
            throw new IllegalArgumentException("POJO collection cannot be null or empty");
        }

        // Validate all POJOs in the collection
        pojos.forEach(this::validatePojo);

        // Convert to List for the InfluxDB client
        final List<T> pojoList = pojos instanceof List ? (List<T>) pojos : List.copyOf(pojos);

        // Use the List-based method for consistency
        importBatch(pojoList);
    }

    /**
     * Imports a list of POJOs into InfluxDB in batch.
     *
     * <p>This method is optimized for List parameters and provides the most efficient
     * batch import operation. For other collection types, use {@link #importBatch(Collection)}.</p>
     *
     * @param pojos the list of POJOs to import
     * @throws IllegalArgumentException if any POJO is not properly annotated or if the list is empty
     */
    public void importBatch(final List<T> pojos) {
        if (pojos == null || pojos.isEmpty()) {
            throw new IllegalArgumentException("POJO list cannot be null or empty");
        }

        // Validate all POJOs in the collection
        pojos.forEach(this::validatePojo);

        final WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();

        // Write all POJOs in the list
        writeApi.writeMeasurements(config.bucket(), config.organization(),
                                  config.writePrecision(), pojos);

        LOGGER.info("Successfully imported batch of {} POJOs to bucket {}",
                   pojos.size(), config.bucket());
    }

    /**
     * Validates that the POJO is properly annotated for InfluxDB import.
     *
     * @param pojo the POJO to validate
     * @throws IllegalArgumentException if the POJO is not properly annotated
     */
    private void validatePojo(final T pojo) {
        if (pojo == null) {
            throw new IllegalArgumentException("POJO cannot be null");
        }

        final Measurement measurement = pojo.getClass().getAnnotation(Measurement.class);
        if (measurement == null) {
            throw new IllegalArgumentException(
                String.format("POJO class %s must be annotated with @Measurement",
                             pojo.getClass().getSimpleName()));
        }

        if (measurement.name() == null || measurement.name().trim().isEmpty()) {
            throw new IllegalArgumentException(
                String.format("@Measurement annotation on class %s must specify a name",
                             pojo.getClass().getSimpleName()));
        }
    }

}
