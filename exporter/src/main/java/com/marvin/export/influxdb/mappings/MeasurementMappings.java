package com.marvin.export.influxdb.mappings;

/**
 * Contains measurement mappings and field definitions for each bucket type. This class defines the
 * expected structure of data from each InfluxDB bucket.
 */
public class MeasurementMappings {

  // Data type converters for handling different value types
  public static class DataTypeConverter {

    public static Object convertToExpectedType(String field, Object value, String bucketType) {
      if (value == null) {
        return null;
      }

      try {
        return switch (bucketType.toLowerCase()) {
          case "system_metrics" -> convertSystemMetricsValue(field, value);
          case "sensor_data" -> convertSensorDataValue(field, value);
          case "sensor_data_30m" -> convertAggregatedValue(field, value);
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
      if ("humidity".equals(field) || field.contains("percent")) {
        return Double.parseDouble(value.toString());
      }
      // Energy values
      if ("current".equals(field) || "voltage".equals(field)
          || "power".equals(field) || "energy".equals(field)) {
        return Double.parseDouble(value.toString());
      }
      return value;
    }

    private static Object convertAggregatedValue(String field, Object value) {
      // Statistical values
      if (field.contains("mean") || field.contains("average")
          || field.contains("min") || field.contains("max")
          || field.contains("stddev") || field.contains("variance")) {
        return Double.parseDouble(value.toString());
      }
      // Count values
      if (field.contains("count") || field.contains("sum")) {
        return Double.parseDouble(value.toString());
      }
      return value;
    }

    private DataTypeConverter() {
    }
  }
}
