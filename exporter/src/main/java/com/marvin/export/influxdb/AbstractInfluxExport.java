package com.marvin.export.influxdb;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.marvin.export.ExportConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Generic base class for exporting data from InfluxDB user buckets.
 * Follows the template method pattern to provide a consistent export workflow
 * while allowing bucket-specific implementations.
 *
 * @param <T> The type of DTO that will be produced from the InfluxDB data
 */
public abstract class AbstractInfluxExport<T> {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractInfluxExport.class);

    @Autowired
    protected InfluxDBClient influxDBClient;

    @Autowired
    protected ExportConfig exportConfig;

    /**
     * Template method that defines the standard export workflow.
     * This method orchestrates the entire export process.
     *
     * @param startTime The start time for the export range (optional)
     * @param endTime The end time for the export range (optional)
     * @return List of exported DTOs
     */
    public List<T> exportData(Optional<Instant> startTime, Optional<Instant> endTime) {
        try {
            logger.info("Starting export for bucket: {}", getBucketName());

            // Validate configuration
            validateConfiguration();

            // Set default time range if not provided
            Instant actualStartTime = startTime.orElse(getDefaultStartTime());
            Instant actualEndTime = endTime.orElse(getDefaultEndTime());

            logger.info("Exporting data from {} to {}", actualStartTime, actualEndTime);

            // Build the query
            String fluxQuery = buildQuery(actualStartTime, actualEndTime);

            // Execute the query
            List<FluxTable> tables = executeQuery(fluxQuery);

            // Convert records to DTOs
            List<T> result = tables.stream()
                    .flatMap(table -> table.getRecords().stream())
                    .map(record -> convertRecord(record))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());

            logger.info("Successfully exported {} records from bucket: {}", result.size(), getBucketName());
            return result;

        } catch (Exception e) {
            logger.error("Failed to export data from bucket: {}", getBucketName(), e);
            throw new InfluxExportException("Export failed for bucket: " + getBucketName(), e);
        }
    }

    /**
     * Validates that the required configuration is present and valid.
     */
    protected void validateConfiguration() {
        if (exportConfig.getInfluxdbUrl() == null || exportConfig.getInfluxdbUrl().trim().isEmpty()) {
            throw new InfluxExportException("InfluxDB URL is not configured");
        }
        if (exportConfig.getInfluxdbToken() == null || exportConfig.getInfluxdbToken().trim().isEmpty()) {
            throw new InfluxExportException("InfluxDB token is not configured");
        }
        if (exportConfig.getInfluxdbOrg() == null || exportConfig.getInfluxdbOrg().trim().isEmpty()) {
            throw new InfluxExportException("InfluxDB organization is not configured");
        }
        if (!exportConfig.isInfluxdbExportEnabled()) {
            throw new InfluxExportException("InfluxDB export is not enabled");
        }
    }

    /**
     * Executes a Flux query against InfluxDB and returns the results.
     *
     * @param fluxQuery The Flux query to execute
     * @return List of FluxTable containing the query results
     */
    protected List<FluxTable> executeQuery(String fluxQuery) {
        try {
            logger.debug("Executing Flux query: {}", fluxQuery);
            return influxDBClient.getQueryApi().query(fluxQuery, exportConfig.getInfluxdbOrg());
        } catch (Exception e) {
            throw new InfluxExportException("Failed to execute Flux query: " + fluxQuery, e);
        }
    }

    /**
     * Gets the default start time for exports (24 hours ago).
     *
     * @return Default start time
     */
    protected Instant getDefaultStartTime() {
        return Instant.now().minus(24, ChronoUnit.HOURS);
    }

    /**
     * Gets the default end time for exports (now).
     *
     * @return Default end time
     */
    protected Instant getDefaultEndTime() {
        return Instant.now();
    }

    // Abstract methods that must be implemented by concrete classes

    /**
     * Returns the name of the bucket to export data from.
     *
     * @return The bucket name
     */
    protected abstract String getBucketName();

    /**
     * Builds a Flux query for exporting data from the bucket.
     *
     * @param startTime The start time for the export range
     * @param endTime The end time for the export range
     * @return A Flux query string
     */
    protected abstract String buildQuery(Instant startTime, Instant endTime);

    /**
     * Converts a single FluxRecord to a DTO.
     *
     * @param record The FluxRecord to convert
     * @return Optional containing the DTO if conversion was successful, empty otherwise
     */
    protected abstract Optional<T> convertRecord(FluxRecord record);

    /**
     * Returns a description of the data type being exported for logging purposes.
     *
     * @return Description of the export data type
     */
    protected abstract String getDataTypeDescription();

    /**
     * Custom exception for InfluxDB export errors.
     */
    public static class InfluxExportException extends RuntimeException {
        public InfluxExportException(String message) {
            super(message);
        }

        public InfluxExportException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}