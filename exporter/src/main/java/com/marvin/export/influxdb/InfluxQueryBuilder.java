package com.marvin.export.influxdb;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Utility class for building Flux queries for InfluxDB data exports.
 * Provides a fluent API for constructing queries with common patterns.
 */
public class InfluxQueryBuilder {

    private final String bucket;
    private String startTime;
    private String endTime;
    private final List<String> measurementFilters = new ArrayList<>();
    private final List<String> fieldFilters = new ArrayList<>();
    private final List<String> mapFunctions = new ArrayList<>();
    private String sortDirection;
    private boolean keepOriginalColumns = true;

    private InfluxQueryBuilder(String bucket) {
        this.bucket = bucket;
    }

    /**
     * Creates a new query builder for the specified bucket.
     *
     * @param bucket The name of the bucket to query
     * @return A new InfluxQueryBuilder instance
     */
    public static InfluxQueryBuilder from(String bucket) {
        if (bucket == null || bucket.trim().isEmpty()) {
            throw new IllegalArgumentException("Bucket name cannot be null or empty");
        }
        return new InfluxQueryBuilder(bucket);
    }

    /**
     * Sets the time range for the query using Instant objects.
     *
     * @param start The start time
     * @param end The end time
     * @return This builder for method chaining
     */
    public InfluxQueryBuilder timeRange(Instant start, Instant end) {
        this.startTime = start.truncatedTo(ChronoUnit.MILLIS).toString();
        this.endTime = end.truncatedTo(ChronoUnit.MILLIS).toString();
        return this;
    }

    /**
     * Filters by measurement name.
     *
     * @param measurement The measurement name to filter by
     * @return This builder for method chaining
     */
    public InfluxQueryBuilder measurement(String measurement) {
        this.measurementFilters.add(String.format("r._measurement == \"%s\"", measurement));
        return this;
    }

    /**
     * Filters by one of multiple measurement names.
     *
     * @param measurements The measurement names to filter by
     * @return This builder for method chaining
     */
    public InfluxQueryBuilder measurements(String... measurements) {
        final StringJoiner joiner = new StringJoiner(" or ");
        for (String measurement : measurements) {
            joiner.add(String.format("r._measurement == \"%s\"", measurement));
        }
        this.measurementFilters.add("(" + joiner + ")");
        return this;
    }

    /**
     * Filters by field name.
     *
     * @param field The field name to filter by
     * @return This builder for method chaining
     */
    public InfluxQueryBuilder field(String field) {
        this.fieldFilters.add(String.format("r._field == \"%s\"", field));
        return this;
    }

    /**
     * Adds a map function to transform records.
     *
     * @param mapFunction The map function as a string (e.g., "fn: (r) => ({ r with _value: r._value * 2 })")
     * @return This builder for method chaining
     */
    public InfluxQueryBuilder map(String mapFunction) {
        this.mapFunctions.add(String.format("|> map(%s)", mapFunction));
        return this;
    }

    /**
     * Sets the sort order of results.
     *
     * @param direction "asc" for ascending or "desc" for descending
     * @return This builder for method chaining
     */
    public InfluxQueryBuilder sort(String direction) {
        if (!"asc".equalsIgnoreCase(direction) && !"desc".equalsIgnoreCase(direction)) {
            throw new IllegalArgumentException("Sort direction must be 'asc' or 'desc'");
        }
        this.sortDirection = direction.toLowerCase();
        return this;
    }

    /**
     * Keeps only the original columns (removes _start, _stop, _time).
     *
     * @param keepColumns Whether to keep only original columns
     * @return This builder for method chaining
     */
    public InfluxQueryBuilder keepOriginalColumns(boolean keepColumns) {
        this.keepOriginalColumns = keepColumns;
        return this;
    }

    /**
     * Builds the final Flux query string.
     *
     * @return The complete Flux query
     */
    public String build() {
        final StringBuilder query = new StringBuilder();

        // Start with from bucket
        query.append(String.format("from(bucket: \"%s\")", bucket));

        // Add time range
        if (startTime != null && endTime != null) {
            query.append(String.format("\n  |> range(start: %s, stop: %s)", startTime, endTime));
        } else {
            // Default to last 24 hours if no time range specified
            query.append("\n  |> range(start: -24h)");
        }

        // Add filters
        final List<String> allFilters = new ArrayList<>();
        allFilters.addAll(measurementFilters);
        allFilters.addAll(fieldFilters);

        if (!allFilters.isEmpty()) {
            query.append("\n  |> filter(fn: (r) => ");
            query.append(String.join(" and ", allFilters));
            query.append(")");
        }

        // Add map functions
        for (String mapFunction : mapFunctions) {
            query.append("\n  ").append(mapFunction);
        }

        // Add sorting
        if (sortDirection != null) {
            query.append(String.format("\n  |> sort(columns: [\"_time\"], desc: %s)", "desc".equals(sortDirection)));
        }

        // Keep original columns if requested
        if (keepOriginalColumns) {
            query.append("\n  |> keep(columns: [\"_measurement\", \"_field\", \"_value\", \"_time\"])");
        }

        return query.toString();
    }
}