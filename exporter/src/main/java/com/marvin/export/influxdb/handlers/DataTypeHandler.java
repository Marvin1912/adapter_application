package com.marvin.export.influxdb.handlers;

import com.influxdb.query.FluxRecord;
import com.marvin.export.influxdb.dto.SystemMetricsDTO;
import com.marvin.export.influxdb.dto.SensorDataDTO;
import com.marvin.export.influxdb.dto.SensorDataAggregatedDTO;
import com.marvin.export.influxdb.dto.CostsDTO;
import com.marvin.export.influxdb.mappings.MeasurementMappings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Handles the conversion of FluxRecord objects to appropriate DTOs based on data type.
 * This class processes the different data types from each bucket and performs proper type conversion.
 */
public class DataTypeHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataTypeHandler.class);

    /**
     * Converts a FluxRecord to the appropriate DTO based on the bucket name.
     *
     * @param record The FluxRecord to convert
     * @param bucketName The name of the bucket the record came from
     * @return Optional containing the appropriate DTO if conversion was successful
     */
    public static Optional<?> convertRecord(FluxRecord record, String bucketName) {
        try {
            return switch (bucketName.toLowerCase()) {
                case "system_metrics" -> Optional.of(convertToSystemMetricsDTO(record));
                case "sensor_data" -> Optional.of(convertToSensorDataDTO(record));
                case "sensor_data_30m" -> Optional.of(convertToSensorDataAggregatedDTO(record));
                case "costs" -> Optional.of(convertToCostsDTO(record));
                default -> {
                    LOGGER.warn("Unknown bucket name: {}", bucketName);
                    yield Optional.empty();
                }
            };
        } catch (Exception e) {
            LOGGER.error("Failed to convert record from bucket: {}", bucketName, e);
            return Optional.empty();
        }
    }

    /**
     * Converts a FluxRecord to SystemMetricsDTO.
     *
     * @param record the FluxRecord to convert
     * @return SystemMetricsDTO the converted DTO
     */
    private static SystemMetricsDTO convertToSystemMetricsDTO(FluxRecord record) {
        final String measurement = record.getMeasurement();
        final Instant timestamp = record.getTime();

        final Map<String, Object> fields = new HashMap<>();
        final Map<String, String> tags = new HashMap<>();

        // Extract fields and tags from the record
        record.getValues().forEach((key, value) -> {
            if (value != null) {
                if (key.equals("_field")) {
                    fields.put(value.toString(), MeasurementMappings.DataTypeConverter
                        .convertToExpectedType(value.toString(), record.getValue(), "system_metrics"));
                } else if (!key.startsWith("_") && !key.equals("table")) {
                    if (value instanceof Number) {
                        // This is likely a tag value that's numeric
                        tags.put(key, value.toString());
                    } else {
                        tags.put(key, value.toString());
                    }
                }
            }
        });

        // Add the field/value pair for this record
        if (record.getField() != null && record.getValue() != null) {
            fields.put(record.getField(), MeasurementMappings.DataTypeConverter
                .convertToExpectedType(record.getField(), record.getValue(), "system_metrics"));
        }

        return new SystemMetricsDTO(measurement, null, timestamp, fields, tags);
    }

    /**
     * Converts a FluxRecord to SensorDataDTO.
     *
     * @param record the FluxRecord to convert
     * @return SensorDataDTO the converted DTO
     */
    private static SensorDataDTO convertToSensorDataDTO(FluxRecord record) {
        final String measurement = record.getMeasurement();
        final Instant timestamp = record.getTime();

        final Map<String, Object> fields = new HashMap<>();
        final Map<String, String> tags = new HashMap<>();

        // Extract fields and tags from the record
        record.getValues().forEach((key, value) -> {
            if (value != null) {
                if (key.equals("_field")) {
                    fields.put(value.toString(), MeasurementMappings.DataTypeConverter
                        .convertToExpectedType(value.toString(), record.getValue(), "sensor_data"));
                } else if (!key.startsWith("_") && !key.equals("table")) {
                    tags.put(key, value.toString());
                }
            }
        });

        // Add the field/value pair for this record
        if (record.getField() != null && record.getValue() != null) {
            fields.put(record.getField(), MeasurementMappings.DataTypeConverter
                .convertToExpectedType(record.getField(), record.getValue(), "sensor_data"));
        }

        // Extract common sensor attributes from tags
        final String entityId = tags.get("entity_id");
        final String friendlyName = tags.get("friendly_name");
        final String deviceClass = tags.get("device_class");
        final String unitOfMeasurement = tags.get("unit_of_measurement");

        return new SensorDataDTO(measurement, entityId, friendlyName, deviceClass,
                                unitOfMeasurement, timestamp, fields, tags);
    }

    /**
     * Converts a FluxRecord to SensorDataAggregatedDTO.
     *
     * @param record the FluxRecord to convert
     * @return SensorDataAggregatedDTO the converted DTO
     */
    private static SensorDataAggregatedDTO convertToSensorDataAggregatedDTO(FluxRecord record) {
        final String measurement = record.getMeasurement();
        final Instant timestamp = record.getTime();

        final Map<String, Object> fields = new HashMap<>();
        final Map<String, String> tags = new HashMap<>();

        // Extract fields and tags from the record
        record.getValues().forEach((key, value) -> {
            if (value != null) {
                if (key.equals("_field")) {
                    fields.put(value.toString(), MeasurementMappings.DataTypeConverter
                        .convertToExpectedType(value.toString(), record.getValue(), "sensor_data_30m"));
                } else if (!key.startsWith("_") && !key.equals("table")) {
                    tags.put(key, value.toString());
                }
            }
        });

        // Add the field/value pair for this record
        if (record.getField() != null && record.getValue() != null) {
            fields.put(record.getField(), MeasurementMappings.DataTypeConverter
                .convertToExpectedType(record.getField(), record.getValue(), "sensor_data_30m"));
        }

        // Extract common sensor attributes from tags
        final String entityId = tags.get("entity_id");
        final String friendlyName = tags.get("friendly_name");
        final String deviceClass = tags.get("device_class");
        final String unitOfMeasurement = tags.get("unit_of_measurement");

        // For aggregated data, we'll use the record timestamp as window start
        // and estimate window end based on aggregation window tag
        final Instant windowStart = timestamp;
        Instant windowEnd = timestamp;
        final String windowTag = tags.get("window");
        if (windowTag != null) {
            windowEnd = estimateWindowEnd(timestamp, windowTag);
        }

        return new SensorDataAggregatedDTO(measurement, entityId, friendlyName, deviceClass,
                                          unitOfMeasurement, windowStart, windowEnd,
                                          fields, tags);
    }

    /**
     * Converts a FluxRecord to CostsDTO.
     *
     * @param record the FluxRecord to convert
     * @return CostsDTO the converted DTO
     */
    private static CostsDTO convertToCostsDTO(FluxRecord record) {
        final String measurement = record.getMeasurement();
        final Instant timestamp = record.getTime();

        final Map<String, Object> fields = new HashMap<>();
        final Map<String, String> tags = new HashMap<>();

        // Extract fields and tags from the record
        record.getValues().forEach((key, value) -> {
            if (value != null) {
                if (key.equals("_field")) {
                    fields.put(value.toString(), MeasurementMappings.DataTypeConverter
                        .convertToExpectedType(value.toString(), record.getValue(), "costs"));
                } else if (!key.startsWith("_") && !key.equals("table")) {
                    tags.put(key, value.toString());
                }
            }
        });

        // Add the field/value pair for this record
        if (record.getField() != null && record.getValue() != null) {
            fields.put(record.getField(), MeasurementMappings.DataTypeConverter
                .convertToExpectedType(record.getField(), record.getValue(), "costs"));
        }

        // Extract cost attributes from tags
        final String costType = tags.get("cost_type");
        final String category = tags.get("category");
        final String description = tags.get("description");

        // Convert timestamp to LocalDate for cost date
        final LocalDate costDate = timestamp.atZone(ZoneId.systemDefault()).toLocalDate();

        return new CostsDTO(measurement, costType, category, description, costDate,
                           timestamp, fields, tags);
    }

    /**
     * Estimates the window end time based on the aggregation window tag.
     *
     * @param windowStart the starting time of the window
     * @param windowTag the window tag indicating duration (e.g., "30m", "1h", "1d")
     * @return the estimated window end time
     */
    private static Instant estimateWindowEnd(Instant windowStart, String windowTag) {
        try {
            if (windowTag.endsWith("m")) {
                final int minutes = Integer.parseInt(windowTag.substring(0, windowTag.length() - 1));
                return windowStart.plusSeconds(minutes * 60L);
            } else if (windowTag.endsWith("h")) {
                final int hours = Integer.parseInt(windowTag.substring(0, windowTag.length() - 1));
                return windowStart.plusSeconds(hours * 3600L);
            } else if (windowTag.endsWith("d")) {
                final int days = Integer.parseInt(windowTag.substring(0, windowTag.length() - 1));
                return windowStart.plusSeconds(days * 86400L);
            }
        } catch (NumberFormatException e) {
            LOGGER.debug("Could not parse window tag: {}", windowTag);
        }

        // Default to 30 minutes if parsing fails
        return windowStart.plusSeconds(1800L);
    }

    /**
     * Validates that a DTO contains required fields for its type.
     *
     * @param dto the data transfer object to validate
     * @param bucketName the bucket name to determine validation rules
     * @return true if the DTO is valid for its type, false otherwise
     */
    public static boolean validateDTO(Object dto, String bucketName) {
        try {
            return switch (bucketName.toLowerCase()) {
                case "system_metrics" -> validateSystemMetricsDTO((SystemMetricsDTO) dto);
                case "sensor_data" -> validateSensorDataDTO((SensorDataDTO) dto);
                case "sensor_data_30m" -> validateSensorDataAggregatedDTO((SensorDataAggregatedDTO) dto);
                case "costs" -> validateCostsDTO((CostsDTO) dto);
                default -> false;
            };
        } catch (Exception e) {
            LOGGER.debug("DTO validation failed for bucket: {}", bucketName, e);
            return false;
        }
    }

    private static boolean validateSystemMetricsDTO(SystemMetricsDTO dto) {
        return dto.measurement() != null &&
               dto.timestamp() != null &&
               !dto.fields().isEmpty();
    }

    private static boolean validateSensorDataDTO(SensorDataDTO dto) {
        return dto.measurement() != null &&
               dto.timestamp() != null &&
               !dto.fields().isEmpty();
    }

    private static boolean validateSensorDataAggregatedDTO(SensorDataAggregatedDTO dto) {
        return dto.measurement() != null &&
               dto.windowStart() != null &&
               dto.windowEnd() != null &&
               !dto.fields().isEmpty();
    }

    private static boolean validateCostsDTO(CostsDTO dto) {
        return dto.measurement() != null &&
               dto.timestamp() != null &&
               !dto.fields().isEmpty();
    }

    /**
     * Gets the data type description for logging purposes.
     *
     * @param bucketName the bucket name to get description for
     * @return a human-readable description of the data type
     */
    public static String getDataTypeDescription(String bucketName) {
        return switch (bucketName.toLowerCase()) {
            case "system_metrics" -> "System performance metrics (CPU, memory, disk, network)";
            case "sensor_data" -> "IoT sensor data (humidity, energy monitoring from Home Assistant)";
            case "sensor_data_30m" -> "Aggregated sensor data (30-minute averaged values)";
            case "costs" -> "Cost and financial metrics";
            default -> "Unknown data type";
        };
    }
}