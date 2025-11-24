package com.marvin.export.influxdb.services;

import org.springframework.stereotype.Service;

/**
 * Export service for humidity sensor data from the sensor_data bucket. Handles humidity readings from various sensors including Xiaomi Aqara weather sensors.
 */
@Service
public class PowerAggregatedExportService extends PowerExportService {

    private static final String BUCKET_NAME = "sensor_data_30m";

    @Override
    protected String getBucketName() {
        return BUCKET_NAME;
    }

}
