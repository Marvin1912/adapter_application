package com.marvin.app.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Request DTO for InfluxDB export operations.
 * Supports bucket selection and optional time range filtering.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InfluxExportRequest {

    /**
     * List of bucket names to export. If null or empty, all enabled buckets will be exported.
     * Valid values: SYSTEM_METRICS, SENSOR_DATA, SENSOR_DATA_AGGREGATED, COSTS
     */
    private List<String> buckets;

    /**
     * Optional start time for filtering data (ISO-8601 format).
     * If not provided, defaults to 24 hours ago for time-range exports.
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private String startTime;

    /**
     * Optional end time for filtering data (ISO-8601 format).
     * If not provided, defaults to current time for time-range exports.
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private String endTime;

    public InfluxExportRequest() { }

    public InfluxExportRequest(List<String> buckets, String startTime, String endTime) {
        this.buckets = buckets;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public List<String> getBuckets() {
        return buckets;
    }

    public void setBuckets(List<String> buckets) {
        this.buckets = buckets;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}