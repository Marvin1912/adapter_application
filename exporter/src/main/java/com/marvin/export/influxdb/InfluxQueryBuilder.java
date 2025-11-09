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
    private final List<String> tagFilters = new ArrayList<>();
    private final List<String> rangeFilters = new ArrayList<>();
    private final List<String> aggregateFunctions = new ArrayList<>();
    private final List<String> mapFunctions = new ArrayList<>();
    private String sortDirection;
    private Integer limit;
    private Integer offset;
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
     * Sets the time range for the query.
     *
     * @param start The start time (in ISO-8601 format or Flux duration)
     * @param end The end time (in ISO-8601 format or Flux duration)
     * @return This builder for method chaining
     */
    public InfluxQueryBuilder timeRange(String start, String end) {
        this.startTime = start;
        this.endTime = end;
        return this;
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
     * Sets a relative time range from the specified start time to now.
     *
     * @param startDuration The start duration (e.g., "-24h", "-7d")
     * @return This builder for method chaining
     */
    public InfluxQueryBuilder relativeTimeRange(String startDuration) {
        this.startTime = startDuration;
        this.endTime = "now()";
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
        this.measurementFilters.add("(" + joiner.toString() + ")");
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
     * Filters by one of multiple field names.
     *
     * @param fields The field names to filter by
     * @return This builder for method chaining
     */
    public InfluxQueryBuilder fields(String... fields) {
        final StringJoiner joiner = new StringJoiner(" or ");
        for (String field : fields) {
            joiner.add(String.format("r._field == \"%s\"", field));
        }
        this.fieldFilters.add("(" + joiner.toString() + ")");
        return this;
    }

    /**
     * Filters by tag value.
     *
     * @param tagName The tag name
     * @param tagValue The tag value
     * @return This builder for method chaining
     */
    public InfluxQueryBuilder tag(String tagName, String tagValue) {
        this.tagFilters.add(String.format("r.%s == \"%s\"", tagName, tagValue));
        return this;
    }

    /**
     * Filters by tag value using a regex pattern.
     *
     * @param tagName The tag name
     * @param regexPattern The regex pattern to match
     * @return This builder for method chaining
     */
    public InfluxQueryBuilder tagRegex(String tagName, String regexPattern) {
        this.tagFilters.add(String.format("r.%s =~ /%s/", tagName, regexPattern));
        return this;
    }

    /**
     * Filters by field value range.
     *
     * @param field The field name
     * @param minValue The minimum value (inclusive)
     * @param maxValue The maximum value (inclusive)
     * @return This builder for method chaining
     */
    public InfluxQueryBuilder valueRange(String field, Number minValue, Number maxValue) {
        this.rangeFilters.add(String.format("r.%s >= %f and r.%s <= %f", field, minValue, field, maxValue));
        return this;
    }

    /**
     * Filters by field value greater than or equal to specified value.
     *
     * @param field The field name
     * @param minValue The minimum value (inclusive)
     * @return This builder for method chaining
     */
    public InfluxQueryBuilder minValue(String field, Number minValue) {
        this.rangeFilters.add(String.format("r.%s >= %f", field, minValue));
        return this;
    }

    /**
     * Adds an aggregate function to the query.
     *
     * @param function The aggregate function (e.g., "mean", "sum", "count")
     * @param column The column to aggregate
     * @return This builder for method chaining
     */
    public InfluxQueryBuilder aggregate(String function, String column) {
        final String aggregate = String.format(
                "|> aggregateWindow(every: inf, fn: %s, column: \"%s\")",
                function, column
        );
        this.aggregateFunctions.add(aggregate);
        return this;
    }

    /**
     * Adds time-based aggregation with a specified window.
     *
     * @param window The window size (e.g., "1h", "30m", "1d")
     * @param function The aggregate function (e.g., "mean", "sum", "count")
     * @return This builder for method chaining
     */
    public InfluxQueryBuilder timeWindow(String window, String function) {
        final String windowAggregate = String.format(
            "|> aggregateWindow(every: %s, fn: %s, createEmpty: false)", window, function);
        this.aggregateFunctions.add(windowAggregate);
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
     * Sets the maximum number of results to return.
     *
     * @param limitValue The maximum number of results
     * @return This builder for method chaining
     */
    public InfluxQueryBuilder limit(int limitValue) {
        this.limit = limitValue;
        return this;
    }

    /**
     * Sets the number of results to skip.
     *
     * @param offsetValue The number of results to skip
     * @return This builder for method chaining
     */
    public InfluxQueryBuilder offset(int offsetValue) {
        this.offset = offsetValue;
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
        allFilters.addAll(tagFilters);
        allFilters.addAll(rangeFilters);

        if (!allFilters.isEmpty()) {
            query.append("\n  |> filter(fn: (r) => ");
            query.append(String.join(" and ", allFilters));
            query.append(")");
        }

        // Add aggregate functions
        for (String aggregate : aggregateFunctions) {
            query.append("\n  ").append(aggregate);
        }

        // Add map functions
        for (String mapFunction : mapFunctions) {
            query.append("\n  ").append(mapFunction);
        }

        // Add sorting
        if (sortDirection != null) {
            query.append(String.format("\n  |> sort(columns: [\"_time\"], desc: %s)", "desc".equals(sortDirection)));
        }

        // Add limit and offset
        if (limit != null) {
            query.append(String.format("\n  |> limit(n: %d", limit));
            if (offset != null) {
                query.append(String.format(", offset: %d", offset));
            }
            query.append(")");
        } else if (offset != null) {
            query.append(String.format("\n  |> limit(n: 1000, offset: %d)", offset));
        }

        // Keep original columns if requested
        if (keepOriginalColumns) {
            query.append("\n  |> keep(columns: [\"_measurement\", \"_field\", \"_value\", \"_time\"])");
        }

        return query.toString();
    }

    /**
     * Creates a simple query for all data in a bucket within the last 24 hours.
     *
     * @param bucket The bucket name
     * @return A simple Flux query
     */
    public static String simpleQuery(String bucket) {
        return from(bucket).build();
    }

    /**
     * Creates a query for a specific measurement within the last 24 hours.
     *
     * @param bucket The bucket name
     * @param measurement The measurement name
     * @return A Flux query for the specific measurement
     */
    public static String measurementQuery(String bucket, String measurement) {
        return from(bucket).measurement(measurement).build();
    }

    /**
     * Creates a time-range query for all data in a bucket.
     *
     * @param bucket The bucket name
     * @param startTime The start time
     * @param endTime The end time
     * @return A Flux query with the specified time range
     */
    public static String timeRangeQuery(String bucket, Instant startTime, Instant endTime) {
        return from(bucket).timeRange(startTime, endTime).build();
    }
}