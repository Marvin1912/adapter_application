package com.marvin.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Request configuration for exporting InfluxDB bucket data with optional time range filtering")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InfluxExportRequest {

    @Schema(
            description = "Bucket name to export.",
            allowableValues = {"SYSTEM_METRICS", "SENSOR_DATA", "SENSOR_DATA_AGGREGATED", "COSTS"},
            example = "SENSOR_DATA"
    )
    private String bucket;

    @Schema(
            description = "Optional start time for filtering data. If not provided, defaults to 5 years ago (implemented in AbstractInfluxExport).",
            example = "2019-01-15T10:30:00",
            pattern = "yyyy-MM-dd'T'HH:mm:ss",
            type = "string",
            format = "date-time"
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC")
    private String startTime;

    @Schema(
            description = "Optional end time for filtering data. If not provided, defaults to current time (implemented in AbstractInfluxExport).",
            example = "2024-01-15T18:45:00",
            pattern = "yyyy-MM-dd'T'HH:mm:ss",
            type = "string",
            format = "date-time"
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC")
    private String endTime;

}
