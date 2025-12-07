package com.marvin.export.influxdb.handlers;

import com.influxdb.query.FluxRecord;
import com.marvin.export.influxdb.dto.AbstractInfluxData;
import com.marvin.export.influxdb.dto.SensorDataDTO;
import com.marvin.export.influxdb.dto.SystemMetricsDTO;
import com.marvin.export.influxdb.mappings.MeasurementMappings;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
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
                case "system_metrics" -> convertToDTO(record, bucketName, SystemMetricsDTO::new);
                case "sensor_data", "sensor_data_30m" -> convertToDTO(record, bucketName, SensorDataDTO::new);
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

    private static <T extends AbstractInfluxData> AbstractInfluxData convertToDTO(FluxRecord record, String bucketName, Supplier<T> supplier) {
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

        T t = supplier.get();
        t.setMeasurement(measurement);
        t.setTimestamp(timestamp);
        t.setField(field.get());
        t.setTags(tags);

        return t;
    }

    public static boolean validateDTO(Object dto, String bucketName) {
        try {
            return switch (bucketName.toLowerCase()) {
                case "system_metrics" -> validateDTO((SystemMetricsDTO) dto);
                case "sensor_data", "sensor_data_30m" -> validateDTO((SensorDataDTO) dto);
                default -> false;
            };
        } catch (Exception e) {
            LOGGER.debug("DTO validation failed for bucket: {}", bucketName, e);
            return false;
        }
    }

    private static boolean validateDTO(AbstractInfluxData dto) {
        return dto.getMeasurement() != null
            && dto.getTimestamp() != null
            && dto.getField() != null;
    }
}
