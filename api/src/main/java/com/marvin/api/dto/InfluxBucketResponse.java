package com.marvin.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class InfluxBucketResponse {

    private List<InfluxBucketDTO> buckets;

    private String message;

    private boolean success;

    private long timestamp;

    private InfluxBucketResponse(List<InfluxBucketDTO> buckets, String message) {
        this.buckets = buckets;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
        this.success = buckets != null;
    }

    public static InfluxBucketResponse success(List<InfluxBucketDTO> buckets) {
        return new InfluxBucketResponse(buckets, "Available InfluxDB buckets retrieved successfully");
    }

    public static InfluxBucketResponse error(String errorMessage) {
        return new InfluxBucketResponse(null, errorMessage);
    }

    public List<InfluxBucketDTO> getBuckets() {
        return buckets;
    }

    public void setBuckets(List<InfluxBucketDTO> buckets) {
        this.buckets = buckets;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public static class InfluxBucketDTO {
        private String name;

        private String bucketName;

        private String description;

        public InfluxBucketDTO(String name, String bucketName, String description) {
            this.name = name;
            this.bucketName = bucketName;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getBucketName() {
            return bucketName;
        }

        public void setBucketName(String bucketName) {
            this.bucketName = bucketName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
