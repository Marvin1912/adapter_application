package com.marvin.export;

import com.marvin.export.influxdb.services.HumidityAggregatedExportService;
import com.marvin.export.influxdb.services.HumidityExportService;
import com.marvin.export.influxdb.services.PowerAggregatedExportService;
import com.marvin.export.influxdb.services.PowerExportService;
import com.marvin.export.influxdb.services.TemperatureAggregatedExportService;
import com.marvin.export.influxdb.services.TemperatureExportService;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Main orchestrator for exporting data from InfluxDB user buckets. Follows the same patterns as the existing Exporter.java to maintain consistency. Supports
 * selective bucket exports and timestamped output file generation.
 */
@Component
public class InfluxExporter {

    public static final String TEMPERATURE_FILENAME_PREFIX = "temperature";
    public static final String HUMIDITY_FILENAME_PREFIX = "humidity";
    public static final String POWER_FILENAME_PREFIX = "power";
    public static final String TEMPERATURE_AGGREGATED_FILENAME_PREFIX = "temperature_aggregated";
    public static final String HUMIDITY_AGGREGATED_FILENAME_PREFIX = "humidity_aggregated";
    public static final String POWER_AGGREGATED_FILENAME_PREFIX = "power_aggregated";

    private static final Logger LOGGER = LoggerFactory.getLogger(InfluxExporter.class);

    private static final DateTimeFormatter FILE_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private static final String FILE_EXTENSION = ".jsonl";

    private final ExportConfig exportConfig;
    private final ExportFileWriter exportFileWriter;
    private final TemperatureExportService temperatureExportService;
    private final HumidityExportService humidityExportService;
    private final HumidityAggregatedExportService humidityAggregatedExportService;
    private final TemperatureAggregatedExportService temperatureAggregatedExportService;
    private final PowerExportService powerExportService;
    private final PowerAggregatedExportService powerAggregatedExportService;

    public InfluxExporter(
        ExportConfig exportConfig,
        ExportFileWriter exportFileWriter,
        TemperatureExportService temperatureExportService,
        HumidityExportService humidityExportService,
        HumidityAggregatedExportService humidityAggregatedExportService,
        TemperatureAggregatedExportService temperatureAggregatedExportService,
        PowerExportService powerExportService,
        PowerAggregatedExportService powerAggregatedExportService
    ) {
        this.exportConfig = exportConfig;
        this.exportFileWriter = exportFileWriter;
        this.temperatureExportService = temperatureExportService;
        this.humidityExportService = humidityExportService;
        this.humidityAggregatedExportService = humidityAggregatedExportService;
        this.temperatureAggregatedExportService = temperatureAggregatedExportService;
        this.powerExportService = powerExportService;
        this.powerAggregatedExportService = powerAggregatedExportService;
    }

    /**
     * Exports data from specific buckets only.
     *
     * @param bucket    The bucket to export
     * @param startTime The start time to export
     * @param endTime   The end time to export
     * @return List of generated file paths
     */
    public List<Path> exportSelectedBucket(InfluxBucket bucket, Instant startTime, Instant endTime) {
        LOGGER.info("Starting export of selected InfluxDB bucket: {}", bucket);

        final String timestamp = LocalDateTime.now().format(FILE_DATE_TIME_FORMATTER);
        final String influxExportFolder = exportConfig.getCostExportFolder();

        final List<Path> exportedFiles = new ArrayList<>();

        try {
            final Path filePath = switch (bucket) {
                case TEMPERATURE -> exportBucket(createFilePath(influxExportFolder, TEMPERATURE_FILENAME_PREFIX, timestamp),
                    () -> temperatureExportService.exportData(startTime, endTime).stream(), "temperature data");
                case HUMIDITY -> exportBucket(createFilePath(influxExportFolder, HUMIDITY_FILENAME_PREFIX, timestamp),
                    () -> humidityExportService.exportData(startTime, endTime).stream(), "humidity data");
                case POWER -> exportBucket(createFilePath(influxExportFolder, POWER_FILENAME_PREFIX, timestamp),
                    () -> powerExportService.exportData(startTime, endTime).stream(), "power data");
                case POWER_AGGREGATED -> exportBucket(createFilePath(influxExportFolder, POWER_AGGREGATED_FILENAME_PREFIX, timestamp),
                    () -> powerAggregatedExportService.exportData(startTime, endTime).stream(), "aggregated power data");
                case TEMPERATURE_AGGREGATED -> exportBucket(createFilePath(influxExportFolder, TEMPERATURE_AGGREGATED_FILENAME_PREFIX, timestamp),
                    () -> temperatureAggregatedExportService.exportData(startTime, endTime).stream(), "aggregated temperature data");
                case HUMIDITY_AGGREGATED -> exportBucket(createFilePath(influxExportFolder, HUMIDITY_AGGREGATED_FILENAME_PREFIX, timestamp),
                    () -> humidityAggregatedExportService.exportData(startTime, endTime).stream(), "aggregated humidity data");
            };

            exportedFiles.add(filePath);
        } catch (Exception e) {
            LOGGER.error("Failed to export bucket: {}", bucket, e);
        }

        LOGGER.info("Successfully exported {} InfluxDB bucket files", exportedFiles.size());
        return exportedFiles.stream().map(Path::getFileName).toList();
    }

    private Path createFilePath(String folder, String prefix, String timestamp) {
        return Path.of(folder, prefix + '_' + timestamp + FILE_EXTENSION);
    }

    private <T> Path exportBucket(Path path, Supplier<Stream<T>> dataSupplier, String description) {
        LOGGER.info("Exporting {} to file: {}", description, path);

        try {
            final Stream<T> dataStream = dataSupplier.get();
            exportFileWriter.writeFile(path, dataStream);
            LOGGER.info("Successfully exported {} records to {}", description, path);
            return path;
        } catch (Exception e) {
            LOGGER.error("Failed to export {}: {}", description, path, e);
            throw e;
        }
    }

    /**
     * Enumeration of available InfluxDB user buckets for export.
     */
    @Getter
    public enum InfluxBucket {
        TEMPERATURE("sensor_data", "Temperature sensor data"),
        HUMIDITY("sensor_data", "Humidity sensor data"),
        POWER("sensor_data", "Power sensor data"),
        TEMPERATURE_AGGREGATED("sensor_data_30m", "30-minute per hour aggregated temperature data"),
        HUMIDITY_AGGREGATED("sensor_data_30m", "30-minute per hour aggregated humidity data"),
        POWER_AGGREGATED("sensor_data_30m", "30-minute per hour aggregated power data");

        private final String bucketName;
        private final String description;

        InfluxBucket(String bucketName, String description) {
            this.bucketName = bucketName;
            this.description = description;
        }
    }
}
