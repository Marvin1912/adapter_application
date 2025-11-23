package com.marvin.export.influxdb.dto;

import java.util.Map;

/**
 * Data Transfer Object for system metrics data from the system_metrics bucket. Contains CPU, memory, and system performance metrics from host home-server.
 *
 * @param measurement the InfluxDB measurement name
 * @param host        the hostname of the system being monitored
 * @param timestamp   the exact timestamp when the metrics were collected
 * @param field       field name to their metric value
 * @param tags        map of tag names to their values
 */
public record SystemMetricsDTO(
        String measurement,
        String host,
        Long timestamp,
        Object field,
        Map<String, String> tags
) {

}
