package com.marvin.app.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for InfluxDB export operations. Supports bucket selection and optional time range filtering.
 */
@Schema(description = "Request configuration for exporting InfluxDB bucket data with optional time range filtering")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InfluxExportRequest {

    /**
     * List of bucket names to export. If null or empty, all enabled buckets will be exported.
     */
    @Schema(
        description = "List of bucket names to export. If null or empty, all enabled buckets will be exported.",
        allowableValues = {"SYSTEM_METRICS", "SENSOR_DATA", "SENSOR_DATA_AGGREGATED", "COSTS"},
        example = "[\"SENSOR_DATA\", \"SYSTEM_METRICS\"]"
    )
    private List<String> buckets;

    /**
     * Optional start time for filtering data. If not provided, defaults to 24 hours ago for time-range exports.
     */
    @Schema(
        description = "Optional start time for filtering data. If not provided along with endTime, defaults to last 24 hours (implemented at query level).",
        example = "2024-01-15T10:30:00",
        pattern = "yyyy-MM-dd'T'HH:mm:ss",
        type = "string",
        format = "date-time"
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC")
    private String startTime;

    /**
     * Optional end time for filtering data. If not provided, defaults to current time for time-range exports.
     */
    @Schema(
        description = "Optional end time for filtering data. If not provided along with startTime, defaults to current time (implemented at query level).",
        example = "2024-01-15T18:45:00",
        pattern = "yyyy-MM-dd'T'HH:mm:ss",
        type = "string",
        format = "date-time"
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC")
    private String endTime;

}
