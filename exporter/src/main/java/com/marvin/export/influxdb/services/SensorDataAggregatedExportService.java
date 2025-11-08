package com.marvin.export.influxdb.services;

import com.influxdb.query.FluxRecord;
import com.marvin.export.influxdb.AbstractInfluxExport;
import com.marvin.export.influxdb.InfluxQueryBuilder;
import com.marvin.export.influxdb.dto.SensorDataAggregatedDTO;
import com.marvin.export.influxdb.handlers.DataTypeHandler;
import com.marvin.export.influxdb.mappings.MeasurementMappings;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Export service for aggregated sensor data from the sensor_data_30m bucket.
 * Handles downsampled humidity sensor data averaged to 30-minute intervals
 * for long-term trend analysis.
 */
@Service
public class SensorDataAggregatedExportService extends AbstractInfluxExport<SensorDataAggregatedDTO> {

    private static final String BUCKET_NAME = "sensor_data_30m";
    private static final String DEFAULT_AGGREGATION_WINDOW = "30m";

    @Override
    protected String getBucketName() {
        return BUCKET_NAME;
    }

    @Override
    protected String buildQuery(Instant startTime, Instant endTime) {
        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurements(
                    // Aggregated sensor measurements we want to export
                    "sensor_aggregated", "sensor_mean", "sensor_stats"
                )
                .keepOriginalColumns(true)
                .sort("desc") // Most recent first
                .build();
    }

    @Override
    protected Optional<SensorDataAggregatedDTO> convertRecord(FluxRecord record) {
        try {
            // Use the DataTypeHandler to convert the record
            final Optional<?> converted = DataTypeHandler.convertRecord(record, BUCKET_NAME);
            if (converted.isPresent() && converted.get() instanceof SensorDataAggregatedDTO dto) {
                // Validate the DTO using DataTypeHandler
                if (DataTypeHandler.validateDTO(dto, BUCKET_NAME)) {
                    return Optional.of(dto);
                } else {
                    LOGGER.debug("SensorDataAggregatedDTO validation failed for record: {}", record);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to convert aggregated sensor data record: {}", record, e);
        }
        return Optional.empty();
    }

    @Override
    protected String getDataTypeDescription() {
        return "Aggregated sensor data (30-minute averaged values for trend analysis)";
    }

    /**
     * Builds a query for aggregated humidity sensor data only.
     *
     * @param startTime the start time for the query
     * @param endTime the end time for the query
     * @return the constructed Flux query string for humidity data
     */
    public String buildAggregatedHumidityQuery(Instant startTime, Instant endTime) {
        final String[] humidityFields =
            MeasurementMappings.SensorDataAggregatedMappings.HUMIDITY_AGGREGATED_FIELDS.toArray(new String[0]);
        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurements("sensor_aggregated", "sensor_mean")
                .fields(humidityFields)
                .tag("device_class", "humidity")
                .tag("window", DEFAULT_AGGREGATION_WINDOW)
                .sort("desc")
                .build();
    }

    /**
     * Builds a query for aggregated temperature sensor data only.
     *
     * @param startTime the start time for the query
     * @param endTime the end time for the query
     * @return the constructed Flux query string for temperature data
     */
    public String buildAggregatedTemperatureQuery(Instant startTime, Instant endTime) {
        final String[] temperatureFields =
            MeasurementMappings.SensorDataAggregatedMappings.TEMPERATURE_AGGREGATED_FIELDS.toArray(new String[0]);
        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurements("sensor_aggregated", "sensor_mean")
                .fields(temperatureFields)
                .tagRegex("device_class", "(temperature|thermal)")
                .tag("window", DEFAULT_AGGREGATION_WINDOW)
                .sort("desc")
                .build();
    }

    /**
     * Builds a query for aggregated energy monitoring data only.
     *
     * @param startTime the start time for the query
     * @param endTime the end time for the query
     * @return the constructed Flux query string for energy monitoring data
     */
    public String buildAggregatedEnergyQuery(Instant startTime, Instant endTime) {
        final String[] energyFields =
            MeasurementMappings.SensorDataAggregatedMappings.ENERGY_AGGREGATED_FIELDS.toArray(new String[0]);
        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurements("sensor_aggregated", "sensor_mean")
                .fields(energyFields)
                .tagRegex("device_class", "(power|energy|current|voltage)")
                .tag("window", DEFAULT_AGGREGATION_WINDOW)
                .sort("desc")
                .build();
    }

    /**
     * Builds a query for statistical aggregation data.
     *
     * @param startTime the start time for the query
     * @param endTime the end time for the query
     * @return the constructed Flux query string for statistical aggregation data
     */
    public String buildStatisticalAggregationQuery(Instant startTime, Instant endTime) {
        final String[] aggregationFields =
            MeasurementMappings.SensorDataAggregatedMappings.AGGREGATION_FIELDS.toArray(new String[0]);
        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurements("sensor_stats")
                .fields(aggregationFields)
                .tag("window", DEFAULT_AGGREGATION_WINDOW)
                .sort("desc")
                .build();
    }

    /**
     * Builds a query for a specific aggregation window.
     *
     * @param window the aggregation window to use (e.g., "30m", "1h")
     * @param startTime the start time for the query
     * @param endTime the end time for the query
     * @return the constructed Flux query string for the specified window
     */
    public String buildWindowQuery(String window, Instant startTime, Instant endTime) {
        if (!MeasurementMappings.SensorDataAggregatedMappings.SUPPORTED_WINDOWS.contains(window)) {
            final String errorMsg = "Unsupported aggregation window: " + window +
                ". Supported windows: " + MeasurementMappings.SensorDataAggregatedMappings.SUPPORTED_WINDOWS;
            throw new IllegalArgumentException(errorMsg);
        }

        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurements("sensor_aggregated", "sensor_mean", "sensor_stats")
                .tag("window", window)
                .sort("desc")
                .build();
    }

    /**
     * Builds a query for aggregated sensors by location/room.
     *
     * @param location the location/room to filter sensor data
     * @param startTime the start time for the query
     * @param endTime the end time for the query
     * @return the constructed Flux query string for location-specific sensor data
     */
    public String buildAggregatedLocationQuery(String location, Instant startTime, Instant endTime) {
        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurements("sensor_aggregated", "sensor_mean")
                .tag("location", location)
                .tag("window", DEFAULT_AGGREGATION_WINDOW)
                .sort("desc")
                .build();
    }

    /**
     * Builds a query for aggregated sensors by entity ID.
     *
     * @param entityId the specific entity ID to filter sensor data
     * @param startTime the start time for the query
     * @param endTime the end time for the query
     * @return the constructed Flux query string for entity-specific sensor data
     */
    public String buildAggregatedEntityQuery(String entityId, Instant startTime, Instant endTime) {
        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurements("sensor_aggregated", "sensor_mean")
                .tag("entity_id", entityId)
                .tag("window", DEFAULT_AGGREGATION_WINDOW)
                .sort("desc")
                .build();
    }

    /**
     * Exports aggregated humidity sensor data specifically.
     *
     * @param startTime optional start time for the export, defaults to configured default
     * @param endTime optional end time for the export, defaults to configured default
     * @return list of aggregated humidity sensor data DTOs
     */
    public List<SensorDataAggregatedDTO> exportAggregatedHumidity(Optional<Instant> startTime, Optional<Instant> endTime) {
        final Instant actualStartTime = startTime.orElse(getDefaultStartTime());
        final Instant actualEndTime = endTime.orElse(getDefaultEndTime());

        final String query = buildAggregatedHumidityQuery(actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports aggregated temperature sensor data specifically.
     *
     * @param startTime optional start time for the export, defaults to configured default
     * @param endTime optional end time for the export, defaults to configured default
     * @return list of aggregated temperature sensor data DTOs
     */
    public List<SensorDataAggregatedDTO> exportAggregatedTemperature(Optional<Instant> startTime, Optional<Instant> endTime) {
        final Instant actualStartTime = startTime.orElse(getDefaultStartTime());
        final Instant actualEndTime = endTime.orElse(getDefaultEndTime());

        final String query = buildAggregatedTemperatureQuery(actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports aggregated energy monitoring data specifically.
     *
     * @param startTime optional start time for the export, defaults to configured default
     * @param endTime optional end time for the export, defaults to configured default
     * @return list of aggregated energy monitoring sensor data DTOs
     */
    public List<SensorDataAggregatedDTO> exportAggregatedEnergy(Optional<Instant> startTime, Optional<Instant> endTime) {
        final Instant actualStartTime = startTime.orElse(getDefaultStartTime());
        final Instant actualEndTime = endTime.orElse(getDefaultEndTime());

        final String query = buildAggregatedEnergyQuery(actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports statistical aggregation data specifically.
     *
     * @param startTime optional start time for the export, defaults to configured default
     * @param endTime optional end time for the export, defaults to configured default
     * @return list of statistical aggregation sensor data DTOs
     */
    public List<SensorDataAggregatedDTO> exportStatisticalAggregations(Optional<Instant> startTime, Optional<Instant> endTime) {
        final Instant actualStartTime = startTime.orElse(getDefaultStartTime());
        final Instant actualEndTime = endTime.orElse(getDefaultEndTime());

        final String query = buildStatisticalAggregationQuery(actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports aggregated data for a specific time window.
     *
     * @param window the aggregation window to use (e.g., "30m", "1h")
     * @param startTime optional start time for the export, defaults to configured default
     * @param endTime optional end time for the export, defaults to configured default
     * @return list of aggregated sensor data DTOs for the specified window
     */
    public List<SensorDataAggregatedDTO> exportByAggregationWindow(String window, Optional<Instant> startTime, Optional<Instant> endTime) {
        final Instant actualStartTime = startTime.orElse(getDefaultStartTime());
        final Instant actualEndTime = endTime.orElse(getDefaultEndTime());

        final String query = buildWindowQuery(window, actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports aggregated sensor data for a specific location/room.
     *
     * @param location the location/room to filter sensor data
     * @param startTime optional start time for the export, defaults to configured default
     * @param endTime optional end time for the export, defaults to configured default
     * @return list of aggregated sensor data DTOs for the specified location
     */
    public List<SensorDataAggregatedDTO> exportAggregatedByLocation(String location, Optional<Instant> startTime, Optional<Instant> endTime) {
        final Instant actualStartTime = startTime.orElse(getDefaultStartTime());
        final Instant actualEndTime = endTime.orElse(getDefaultEndTime());

        final String query = buildAggregatedLocationQuery(location, actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports aggregated sensor data for a specific entity.
     *
     * @param entityId the specific entity ID to filter sensor data
     * @param startTime optional start time for the export, defaults to configured default
     * @param endTime optional end time for the export, defaults to configured default
     * @return list of aggregated sensor data DTOs for the specified entity
     */
    public List<SensorDataAggregatedDTO> exportAggregatedByEntity(String entityId, Optional<Instant> startTime, Optional<Instant> endTime) {
        final Instant actualStartTime = startTime.orElse(getDefaultStartTime());
        final Instant actualEndTime = endTime.orElse(getDefaultEndTime());

        final String query = buildAggregatedEntityQuery(entityId, actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports aggregated data for the last 24 hours with 30-minute windows.
     *
     * @return list of aggregated sensor data DTOs for the last 24 hours
     */
    public List<SensorDataAggregatedDTO> exportLast24Hours() {
        final Instant endTime = Instant.now();
        final Instant startTime = endTime.minusSeconds(24 * 60 * 60); // 24 hours ago

        return exportData(Optional.of(startTime), Optional.of(endTime));
    }

    /**
     * Exports aggregated data for the last 7 days with 30-minute windows.
     *
     * @return list of aggregated sensor data DTOs for the last 7 days
     */
    public List<SensorDataAggregatedDTO> exportLast7Days() {
        final Instant endTime = Instant.now();
        final Instant startTime = endTime.minusSeconds(7 * 24 * 60 * 60); // 7 days ago

        final String query = InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurements("sensor_aggregated", "sensor_mean")
                .tag("window", DEFAULT_AGGREGATION_WINDOW)
                .sort("desc")
                .build();

        return executeQueryAndConvert(query);
    }

    /**
     * Helper method to execute a query and convert results.
     *
     * @param query the Flux query to execute
     * @return list of converted aggregated sensor data DTOs
     */
    private List<SensorDataAggregatedDTO> executeQueryAndConvert(String query) {
        try {
            return executeQuery(query).stream()
                    .flatMap(table -> table.getRecords().stream())
                    .map(this::convertRecord)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();
        } catch (Exception e) {
            LOGGER.error("Failed to execute aggregated sensor data query: {}", query, e);
            throw new InfluxExportException("Failed to execute aggregated sensor data query", e);
        }
    }
}