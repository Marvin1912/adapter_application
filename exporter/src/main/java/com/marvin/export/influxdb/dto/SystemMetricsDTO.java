package com.marvin.export.influxdb.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

/**
 * Data Transfer Object for system metrics data from the system_metrics bucket.
 * Contains CPU, memory, and system performance metrics from host home-server.
 *
 * @param measurement the InfluxDB measurement name
 * @param host the hostname of the system being monitored
 * @param timestamp the exact timestamp when the metrics were collected
 * @param fields map of field names to their metric values
 * @param tags map of tag names to their values
 */
public record SystemMetricsDTO(
    String measurement,
    String host,
    Instant timestamp,
    Map<String, Object> fields,
    Map<String, String> tags
) {
    // Common system metrics field getters
    public BigDecimal getCpuUsage() {
        final Object value = fields.get("cpu_usage");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getMemoryUsage() {
        final Object value = fields.get("memory_usage");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getMemoryTotal() {
        final Object value = fields.get("memory_total");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getMemoryAvailable() {
        final Object value = fields.get("memory_available");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getLoadAverage() {
        final Object value = fields.get("load_average");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public Long getProcessCount() {
        final Object value = fields.get("process_count");
        return value != null ? Long.valueOf(value.toString()) : null;
    }

    public BigDecimal getDiskUsage() {
        final Object value = fields.get("disk_usage");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getDiskTotal() {
        final Object value = fields.get("disk_total");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getNetworkBytesIn() {
        final Object value = fields.get("network_bytes_in");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getNetworkBytesOut() {
        final Object value = fields.get("network_bytes_out");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    // Common tag getters
    public String getHost() {
        return tags != null ? tags.get("host") : null;
    }

    public String getHostname() {
        return tags != null ? tags.get("hostname") : null;
    }

    public String getCpu() {
        return tags != null ? tags.get("cpu") : null;
    }

    public String getDevice() {
        return tags != null ? tags.get("device") : null;
    }

    public String getInterface() {
        return tags != null ? tags.get("interface") : null;
    }
}