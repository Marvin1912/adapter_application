package com.marvin.influxdb.core;

import com.influxdb.client.domain.WritePrecision;

/**
 * Configuration class for InfluxDB write operations.
 *
 * <p>This class holds configuration settings for writing data to InfluxDB,
 * including bucket, organization, and write precision settings.</p>
 *
 * @param bucket        the InfluxDB bucket to write to
 * @param organization  the InfluxDB organization
 * @param writePrecision the write precision to use for timestamps
 * @author Marvin Application
 * @version 1.0
 * @since 1.0
 */
public record InfluxWriteConfig(
        String bucket,
        String organization,
        WritePrecision writePrecision
) {

    /**
     * Creates a default configuration with nanosecond precision.
     *
     * @param bucket       the InfluxDB bucket
     * @param organization the InfluxDB organization
     * @return a new InfluxWriteConfig with default settings
     */
    public static InfluxWriteConfig createDefault(String bucket, String organization) {
        return new InfluxWriteConfig(bucket, organization, WritePrecision.NS);
    }

    /**
     * Creates a configuration with the specified write precision.
     *
     * @param bucket        the InfluxDB bucket
     * @param organization  the InfluxDB organization
     * @param writePrecision the write precision to use
     * @return a new InfluxWriteConfig with the specified settings
     */
    public static InfluxWriteConfig create(String bucket, String organization, WritePrecision writePrecision) {
        return new InfluxWriteConfig(bucket, organization, writePrecision);
    }

    /**
     * Creates a configuration based on the existing cost import settings.
     *
     * @return a new InfluxWriteConfig with cost bucket settings
     */
    public static InfluxWriteConfig forCostBucket() {
        return createDefault("costs", "wildfly_domain");
    }
}