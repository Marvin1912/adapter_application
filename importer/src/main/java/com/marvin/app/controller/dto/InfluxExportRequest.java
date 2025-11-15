package com.marvin.app.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for InfluxDB export operations. Supports bucket selection and optional time range
 * filtering.
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InfluxExportRequest {

  /**
   * List of bucket names to export. If null or empty, all enabled buckets will be exported. Valid
   * values: SYSTEM_METRICS, SENSOR_DATA, SENSOR_DATA_AGGREGATED, COSTS
   */
  private List<String> buckets;

  /**
   * Optional start time for filtering data (ISO-8601 format). If not provided, defaults to 24 hours
   * ago for time-range exports.
   */
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Europe/Berlin'", timezone = "UTC")
  private String startTime;

  /**
   * Optional end time for filtering data (ISO-8601 format). If not provided, defaults to current
   * time for time-range exports.
   */
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Europe/Berlin'", timezone = "UTC")
  private String endTime;

}
