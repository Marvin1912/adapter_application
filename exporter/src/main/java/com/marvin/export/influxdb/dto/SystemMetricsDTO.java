package com.marvin.export.influxdb.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

/**
 * Data Transfer Object for system metrics data from the system_metrics bucket.
 * Contains CPU, memory, and system performance metrics from host home-server.
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
        Object value = fields.get("cpu_usage");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getMemoryUsage() {
        Object value = fields.get("memory_usage");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getMemoryTotal() {
        Object value = fields.get("memory_total");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getMemoryAvailable() {
        Object value = fields.get("memory_available");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getLoadAverage() {
        Object value = fields.get("load_average");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public Long getProcessCount() {
        Object value = fields.get("process_count");
        return value != null ? Long.valueOf(value.toString()) : null;
    }

    public BigDecimal getDiskUsage() {
        Object value = fields.get("disk_usage");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getDiskTotal() {
        Object value = fields.get("disk_total");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getNetworkBytesIn() {
        Object value = fields.get("network_bytes_in");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getNetworkBytesOut() {
        Object value = fields.get("network_bytes_out");
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