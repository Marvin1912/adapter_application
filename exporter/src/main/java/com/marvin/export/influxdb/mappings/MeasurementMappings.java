package com.marvin.export.influxdb.mappings;

import com.marvin.export.influxdb.dto.SystemMetricsDTO;
import com.marvin.export.influxdb.dto.SensorDataDTO;
import com.marvin.export.influxdb.dto.SensorDataAggregatedDTO;
import com.marvin.export.influxdb.dto.CostsDTO;
import java.time.Instant;
import java.util.*;

/**
 * Contains measurement mappings and field definitions for each bucket type.
 * This class defines the expected structure of data from each InfluxDB bucket.
 */
public class MeasurementMappings {

    // System Metrics Bucket Mappings
    public static final class SystemMetricsMappings {
        public static final Set<String> COMMON_MEASUREMENTS = Set.of(
            "cpu", "mem", "system", "disk", "diskio", "net", "processes", "swap"
        );

        public static final Set<String> CPU_FIELDS = Set.of(
            "usage_user", "usage_system", "usage_idle", "usage_iowait", "usage_steal",
            "usage_guest", "usage_guest_nice", "cpu_usage"
        );

        public static final Set<String> MEMORY_FIELDS = Set.of(
            "active", "available", "available_percent", "buffered", "cached", "commit_limit",
            "committed_as", "dirty", "free", "huge_pages_free", "huge_pages_total",
            "inactive", "mapped", "memory_usage", "shared", "slab", "swap_cached",
            "swap_free", "swap_total", "total", "used", "used_percent", "vmalloc_chunk",
            "vmalloc_total", "vmalloc_used", "write_back", "write_back_tmp"
        );

        public static final Set<String> SYSTEM_FIELDS = Set.of(
            "load1", "load5", "load15", "load_average", "n_cpus", "n_users", "uptime",
            "uptime_format"
        );

        public static final Set<String> DISK_FIELDS = Set.of(
            "free", "inodes_free", "inodes_total", "inodes_used", "total", "used",
            "used_percent", "disk_usage", "disk_total"
        );

        public static final Set<String> NETWORK_FIELDS = Set.of(
            "bytes_sent", "bytes_recv", "packets_sent", "packets_recv", "err_in",
            "err_out", "drop_in", "drop_out", "network_bytes_in", "network_bytes_out"
        );

        public static final Set<String> PROCESS_FIELDS = Set.of(
            "count", "running", "sleeping", "thread_count", "process_count"
        );

        public static final Set<String> COMMON_TAGS = Set.of(
            "host", "hostname", "cpu", "device", "interface", "path", "mode", "state"
        );
    }

    // Sensor Data Bucket Mappings
    public static final class SensorDataMappings {
        public static final Set<String> COMMON_MEASUREMENTS = Set.of(
            "sensor", "binary_sensor", "climate", "energy", "power"
        );

        public static final Set<String> HUMIDITY_FIELDS = Set.of(
            "humidity", "value", "temperature"
        );

        public static final Set<String> ENERGY_FIELDS = Set.of(
            "current", "voltage", "power", "energy", "apparent_power", "power_factor",
            "frequency", "value"
        );

        public static final Set<String> COMMON_FIELDS = Set.of(
            "value", "state", "status"
        );

        public static final Set<String> COMMON_TAGS = Set.of(
            "entity_id", "friendly_name", "device_class", "unit_of_measurement",
            "source", "device", "sensor_type", "location", "room", "area"
        );

        public static final Map<String, Set<String>> DEVICE_TYPE_MAPPINGS = Map.of(
            "xiaomi_aqara", Set.of("bathroom", "hallway", "kitchen", "bedroom", "living_room"),
            "tasmota", Set.of("energy_monitor", "power_monitor", "switch"),
            "home_assistant", Set.of("sensor", "climate", "energy")
        );
    }

    // Aggregated Sensor Data Bucket Mappings
    public static final class SensorDataAggregatedMappings {
        public static final Set<String> COMMON_MEASUREMENTS = Set.of(
            "sensor_aggregated", "sensor_mean", "sensor_stats"
        );

        public static final Set<String> AGGREGATION_FIELDS = Set.of(
            "mean", "average", "min", "max", "sum", "count", "stddev", "variance"
        );

        public static final Set<String> HUMIDITY_AGGREGATED_FIELDS = Set.of(
            "humidity_mean", "humidity_min", "humidity_max", "humidity_sum", "humidity_count"
        );

        public static final Set<String> TEMPERATURE_AGGREGATED_FIELDS = Set.of(
            "temperature_mean", "temperature_min", "temperature_max", "temperature_sum", "temperature_count"
        );

        public static final Set<String> ENERGY_AGGREGATED_FIELDS = Set.of(
            "power_mean", "power_min", "power_max", "energy_sum", "current_mean",
            "voltage_mean", "frequency_mean"
        );

        public static final Set<String> COMMON_TAGS = Set.of(
            "entity_id", "friendly_name", "device_class", "unit_of_measurement",
            "source", "device", "sensor_type", "location", "room", "area", "window", "aggregation"
        );

        public static final String DEFAULT_AGGREGATION_WINDOW = "30m";
        public static final Set<String> SUPPORTED_WINDOWS = Set.of("5m", "15m", "30m", "1h", "6h", "1d");
    }

    // Costs Bucket Mappings
    public static final class CostsMappings {
        public static final Set<String> COMMON_MEASUREMENTS = Set.of(
            "costs", "expenses", "billing", "subscriptions", "licenses"
        );

        public static final Set<String> COST_FIELDS = Set.of(
            "value", "amount", "cost", "base_cost", "variable_cost", "fixed_cost",
            "tax", "rate", "price_per_unit"
        );

        public static final Set<String> USAGE_FIELDS = Set.of(
            "usage", "consumption", "quantity", "volume", "hours", "units"
        );

        public static final Set<String> COST_TYPES = Set.of(
            "energy", "infrastructure", "license", "maintenance", "operational",
            "subscription", "service", "support", "hosting", "software"
        );

        public static final Set<String> COST_CATEGORIES = Set.of(
            "electricity", "hosting", "server", "software", "support", "maintenance",
            "monitoring", "backup", "security", "network", "storage", "database"
        );

        public static final Set<String> COMMON_TAGS = Set.of(
            "cost_type", "category", "currency", "billing_period", "provider",
            "service", "account", "region", "environment"
        );

        public static final String DEFAULT_CURRENCY = "EUR";
        public static final Set<String> SUPPORTED_CURRENCIES = Set.of("EUR", "USD", "GBP", "CHF");
    }

    // Helper methods for field validation and mapping
    public static class FieldValidator {

        public static boolean isValidSystemMetricsField(String field) {
            return SystemMetricsMappings.CPU_FIELDS.contains(field) ||
                   SystemMetricsMappings.MEMORY_FIELDS.contains(field) ||
                   SystemMetricsMappings.SYSTEM_FIELDS.contains(field) ||
                   SystemMetricsMappings.DISK_FIELDS.contains(field) ||
                   SystemMetricsMappings.NETWORK_FIELDS.contains(field) ||
                   SystemMetricsMappings.PROCESS_FIELDS.contains(field);
        }

        public static boolean isValidSensorDataField(String field) {
            return SensorDataMappings.HUMIDITY_FIELDS.contains(field) ||
                   SensorDataMappings.ENERGY_FIELDS.contains(field) ||
                   SensorDataMappings.COMMON_FIELDS.contains(field);
        }

        public static boolean isValidAggregatedField(String field) {
            return SensorDataAggregatedMappings.AGGREGATION_FIELDS.contains(field) ||
                   SensorDataAggregatedMappings.HUMIDITY_AGGREGATED_FIELDS.contains(field) ||
                   SensorDataAggregatedMappings.TEMPERATURE_AGGREGATED_FIELDS.contains(field) ||
                   SensorDataAggregatedMappings.ENERGY_AGGREGATED_FIELDS.contains(field);
        }

        public static boolean isValidCostField(String field) {
            return CostsMappings.COST_FIELDS.contains(field) ||
                   CostsMappings.USAGE_FIELDS.contains(field);
        }

        public static boolean isValidTag(String tag, String bucketType) {
            return switch (bucketType.toLowerCase()) {
                case "system_metrics" -> SystemMetricsMappings.COMMON_TAGS.contains(tag);
                case "sensor_data" -> SensorDataMappings.COMMON_TAGS.contains(tag);
                case "sensor_data_30m" -> SensorDataAggregatedMappings.COMMON_TAGS.contains(tag);
                case "costs" -> CostsMappings.COMMON_TAGS.contains(tag);
                default -> false;
            };
        }
    }

    // Data type converters for handling different value types
    public static class DataTypeConverter {

        public static Object convertToExpectedType(String field, Object value, String bucketType) {
            if (value == null) return null;

            try {
                return switch (bucketType.toLowerCase()) {
                    case "system_metrics" -> convertSystemMetricsValue(field, value);
                    case "sensor_data" -> convertSensorDataValue(field, value);
                    case "sensor_data_30m" -> convertAggregatedValue(field, value);
                    case "costs" -> convertCostValue(field, value);
                    default -> value;
                };
            } catch (Exception e) {
                // Return original value if conversion fails
                return value;
            }
        }

        private static Object convertSystemMetricsValue(String field, Object value) {
            // CPU and memory percentages
            if (field.contains("percent") || field.contains("usage")) {
                return Double.parseDouble(value.toString());
            }
            // Count values
            if (field.contains("count") || field.contains("n_") || field.contains("total")) {
                return Long.parseLong(value.toString());
            }
            // Load averages and rates
            if (field.contains("load") || field.contains("rate")) {
                return Double.parseDouble(value.toString());
            }
            return value;
        }

        private static Object convertSensorDataValue(String field, Object value) {
            // Percentage values
            if (field.equals("humidity") || field.contains("percent")) {
                return Double.parseDouble(value.toString());
            }
            // Energy values
            if (field.equals("current") || field.equals("voltage") ||
                field.equals("power") || field.equals("energy")) {
                return Double.parseDouble(value.toString());
            }
            return value;
        }

        private static Object convertAggregatedValue(String field, Object value) {
            // Statistical values
            if (field.contains("mean") || field.contains("average") ||
                field.contains("min") || field.contains("max") ||
                field.contains("stddev") || field.contains("variance")) {
                return Double.parseDouble(value.toString());
            }
            // Count values
            if (field.contains("count") || field.contains("sum")) {
                return Double.parseDouble(value.toString());
            }
            return value;
        }

        private static Object convertCostValue(String field, Object value) {
            // Monetary values
            if (field.equals("value") || field.equals("amount") || field.equals("cost") ||
                field.contains("cost") || field.contains("rate") || field.contains("price")) {
                return Double.parseDouble(value.toString());
            }
            // Usage values
            if (field.equals("usage") || field.equals("consumption") || field.equals("quantity")) {
                return Double.parseDouble(value.toString());
            }
            return value;
        }
    }
}