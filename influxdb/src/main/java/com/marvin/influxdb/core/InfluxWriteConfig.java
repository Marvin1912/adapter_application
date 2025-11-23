package com.marvin.influxdb.core;

import com.influxdb.client.domain.WritePrecision;

public record InfluxWriteConfig(
        String bucket,
        String organization,
        WritePrecision writePrecision
) {

    public static InfluxWriteConfig create(String bucket, String organization, WritePrecision writePrecision) {
        return new InfluxWriteConfig(bucket, organization, writePrecision);
    }

}