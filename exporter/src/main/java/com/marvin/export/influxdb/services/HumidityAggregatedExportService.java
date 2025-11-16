package com.marvin.export.influxdb.services;

import org.springframework.stereotype.Service;

/**
 * Export service for aggregated sensor data from the sensor_data_30m bucket. Handles downsampled humidity sensor data averaged to 30-minute intervals for
 * long-term trend analysis.
 */
@Service
public class HumidityAggregatedExportService extends HumidityExportService {

    private static final String BUCKET_NAME = "sensor_data_30m";

    @Override
    protected String getBucketName() {
        return BUCKET_NAME;
    }
}
