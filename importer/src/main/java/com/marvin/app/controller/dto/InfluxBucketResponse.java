package com.marvin.app.controller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * Response DTO for InfluxDB bucket information. Provides details about available buckets for export.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InfluxBucketResponse {

    /**
     * List of available InfluxDB buckets with their details.
     */
    private List<InfluxBucketDTO> buckets;

    /**
     * Status message.
     */
    private String message;

    /**
     * Timestamp when the response was generated.
     */
    private long timestamp;

    private InfluxBucketResponse(List<InfluxBucketDTO> buckets, String message) {
        this.buckets = buckets;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Creates a successful bucket response.
     *
     * @param buckets List of available buckets
     * @return Successful response
     */
    public static InfluxBucketResponse success(List<InfluxBucketDTO> buckets) {
        return new InfluxBucketResponse(buckets, "Available InfluxDB buckets retrieved successfully");
    }

    /**
     * Creates an error bucket response.
     *
     * @param errorMessage Error message
     * @return Error response
     */
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

    /**
     * DTO representing an individual InfluxDB bucket.
     */
    public static class InfluxBucketDTO {
        /**
         * The enum name of the bucket (e.g., SYSTEM_METRICS).
         */
        private String name;

        /**
         * The actual bucket name in InfluxDB (e.g., system_metrics).
         */
        private String bucketName;

        /**
         * Description of what data this bucket contains.
         */
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