package com.marvin.export.influxdb.handlers;

import com.influxdb.query.FluxRecord;
import com.marvin.export.influxdb.dto.SensorDataDTO;
import com.marvin.export.influxdb.dto.SystemMetricsDTO;
import com.marvin.export.influxdb.mappings.MeasurementMappings;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the conversion of FluxRecord objects to appropriate DTOs based on data type. This class processes the different data types from each bucket and
 * performs proper type conversion.
 */
public class DataTypeHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataTypeHandler.class);

    private DataTypeHandler() {
    }

    /**
     * Converts a FluxRecord to the appropriate DTO based on the bucket name.
     *
     * @param record     The FluxRecord to convert
     * @param bucketName The name of the bucket the record came from
     * @return the appropriate DTO if conversion was successful, null otherwise
     */
    public static Object convertRecord(FluxRecord record, String bucketName) {
        try {
            return switch (bucketName.toLowerCase()) {
                case "system_metrics" -> convertToSystemMetricsDTO(record);
                case "sensor_data", "sensor_data_30m" -> convertToSensorDataDTO(record);
                default -> {
                    LOGGER.warn("Unknown bucket name: {}", bucketName);
                    yield null;
                }
            };
        } catch (Exception e) {
            LOGGER.error("Failed to convert record from bucket: {}", bucketName, e);
            return null;
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
                if ("_field".equals(key)) {
                    fields.put(value.toString(), MeasurementMappings.DataTypeConverter
                            .convertToExpectedType(value.toString(), record.getValue(), "system_metrics"));
                } else if (!key.startsWith("_") && !"table".equals(key)) {
                    tags.put(key, value.toString());
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
                if ("_field".equals(key)) {
                    fields.put(value.toString(), MeasurementMappings.DataTypeConverter
                            .convertToExpectedType(value.toString(), record.getValue(), "sensor_data"));
                } else if (!key.startsWith("_") && !"table".equals(key)) {
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

        return new SensorDataDTO(measurement, entityId, friendlyName, timestamp, fields, tags);
    }

    public static boolean validateDTO(Object dto, String bucketName) {
        try {
            return switch (bucketName.toLowerCase()) {
                case "system_metrics" -> validateSystemMetricsDTO((SystemMetricsDTO) dto);
                case "sensor_data", "sensor_data_30m" -> validateSensorDataDTO((SensorDataDTO) dto);
                default -> false;
            };
        } catch (Exception e) {
            LOGGER.debug("DTO validation failed for bucket: {}", bucketName, e);
            return false;
        }
    }

    private static boolean validateSystemMetricsDTO(SystemMetricsDTO dto) {
        return dto.measurement() != null
                && dto.timestamp() != null
                && !dto.fields().isEmpty();
    }

    private static boolean validateSensorDataDTO(SensorDataDTO dto) {
        return dto.measurement() != null
                && dto.timestamp() != null
                && !dto.fields().isEmpty();
    }
}
