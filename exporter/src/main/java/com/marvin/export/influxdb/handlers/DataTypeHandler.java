package com.marvin.export.influxdb.handlers;

import com.influxdb.query.FluxRecord;
import com.marvin.export.influxdb.dto.SensorDataDTO;
import com.marvin.export.influxdb.dto.SystemMetricsDTO;
import com.marvin.export.influxdb.mappings.MeasurementMappings;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
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
                case "system_metrics" -> convertToSystemMetricsDTO(record, bucketName);
                case "sensor_data", "sensor_data_30m" -> convertToSensorDataDTO(record, bucketName);
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

    private static SystemMetricsDTO convertToSystemMetricsDTO(FluxRecord record, String bucketName) {
        final String measurement = record.getMeasurement();
        final Long timestamp = Optional.ofNullable(record.getTime()).map(Instant::toEpochMilli).orElseThrow();

        final AtomicReference<Object> field = new AtomicReference<>();
        final Map<String, String> tags = new HashMap<>();

        record.getValues().forEach((key, value) -> {
            if (value != null) {
                if ("_field".equals(key)) {
                    field.set(MeasurementMappings.DataTypeConverter.convertToExpectedType(value.toString(), record.getValue(), bucketName));
                } else if (!key.startsWith("_") && !"table".equals(key)) {
                    tags.put(key, value.toString());
                }
            }
        });

        if (record.getField() != null && record.getValue() != null) {
            field.set(MeasurementMappings.DataTypeConverter.convertToExpectedType(record.getField(), record.getValue(), bucketName));
        }

        return new SystemMetricsDTO(measurement, null, timestamp, field.get(), tags);
    }

    private static SensorDataDTO convertToSensorDataDTO(FluxRecord record, String bucketName) {
        final String measurement = record.getMeasurement();
        final Long timestamp = Optional.ofNullable(record.getTime()).map(Instant::toEpochMilli).orElseThrow();

        final AtomicReference<Object> field = new AtomicReference<>();
        final Map<String, String> tags = new HashMap<>();

        record.getValues().forEach((key, value) -> {
            if (value != null) {
                if ("_field".equals(key)) {
                    field.set(MeasurementMappings.DataTypeConverter.convertToExpectedType(value.toString(), record.getValue(), bucketName));
                } else if (!key.startsWith("_") && !"table".equals(key)) {
                    tags.put(key, value.toString());
                }
            }
        });

        if (record.getField() != null && record.getValue() != null) {
            field.set(MeasurementMappings.DataTypeConverter.convertToExpectedType(record.getField(), record.getValue(), bucketName));
        }

        final String entityId = tags.get("entity_id");
        final String friendlyName = tags.get("friendly_name");

        return new SensorDataDTO(measurement, entityId, friendlyName, timestamp, field.get(), tags);
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
            && dto.field() != null;
    }

    private static boolean validateSensorDataDTO(SensorDataDTO dto) {
        return dto.measurement() != null
            && dto.timestamp() != null
            && dto.field() != null;
    }
}
