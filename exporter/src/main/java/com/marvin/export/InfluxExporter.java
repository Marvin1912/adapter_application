package com.marvin.export;

import com.marvin.export.influxdb.services.SystemMetricsExportService;
import com.marvin.export.influxdb.services.SensorDataExportService;
import com.marvin.export.influxdb.services.SensorDataAggregatedExportService;
import com.marvin.export.influxdb.services.CostsExportService;
import com.marvin.export.influxdb.dto.SystemMetricsDTO;
import com.marvin.export.influxdb.dto.SensorDataDTO;
import com.marvin.export.influxdb.dto.SensorDataAggregatedDTO;
import com.marvin.export.influxdb.dto.CostsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Main orchestrator for exporting data from InfluxDB user buckets.
 * Follows the same patterns as the existing Exporter.java to maintain consistency.
 * Supports selective bucket exports and timestamped output file generation.
 */
@Component
public class InfluxExporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfluxExporter.class);

    private static final DateTimeFormatter FILE_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private static final String SYSTEM_METRICS_FILENAME_PREFIX = "system_metrics_";
    private static final String SENSOR_DATA_FILENAME_PREFIX = "sensor_data_";
    private static final String SENSOR_DATA_AGGREGATED_FILENAME_PREFIX = "sensor_data_30m_";
    private static final String COSTS_FILENAME_PREFIX = "costs_";
    private static final String FILE_EXTENSION = ".json";

    private final ExportConfig exportConfig;
    private final ExportFileWriter exportFileWriter;
    private final SystemMetricsExportService systemMetricsExportService;
    private final SensorDataExportService sensorDataExportService;
    private final SensorDataAggregatedExportService sensorDataAggregatedExportService;
    private final CostsExportService costsExportService;

    public InfluxExporter(
            ExportConfig exportConfig,
            ExportFileWriter exportFileWriter,
            SystemMetricsExportService systemMetricsExportService,
            SensorDataExportService sensorDataExportService,
            SensorDataAggregatedExportService sensorDataAggregatedExportService,
            CostsExportService costsExportService) {
        this.exportConfig = exportConfig;
        this.exportFileWriter = exportFileWriter;
        this.systemMetricsExportService = systemMetricsExportService;
        this.sensorDataExportService = sensorDataExportService;
        this.sensorDataAggregatedExportService = sensorDataAggregatedExportService;
        this.costsExportService = costsExportService;
    }

    /**
     * Exports data from all enabled user buckets.
     *
     * @return List of generated file paths
     */
    public List<Path> exportAllBuckets() {
        LOGGER.info("Starting export of all InfluxDB user buckets");

        if (!exportConfig.isInfluxdbExportEnabled()) {
            LOGGER.warn("InfluxDB export is disabled in configuration");
            return Collections.emptyList();
        }

        return exportSelectedBuckets(EnumSet.allOf(InfluxBucket.class));
    }

    /**
     * Exports data from specific buckets only.
     *
     * @param buckets The buckets to export
     * @return List of generated file paths
     */
    public List<Path> exportSelectedBuckets(Set<InfluxBucket> buckets) {
        LOGGER.info("Starting export of selected InfluxDB buckets: {}", buckets);

        final String timestamp = LocalDateTime.now().format(FILE_DATE_TIME_FORMATTER);
        final String influxExportFolder = exportConfig.getCostExportFolder(); // Reuse same folder

        List<Path> exportedFiles = new ArrayList<>();

        for (InfluxBucket bucket : buckets) {
            try {
                Path filePath = switch (bucket) {
                    case SYSTEM_METRICS -> exportBucket(
                            createFilePath(influxExportFolder, SYSTEM_METRICS_FILENAME_PREFIX, timestamp),
                            () -> systemMetricsExportService.exportData(Optional.empty(), Optional.empty()).stream(),
                            "system metrics"
                    );
                    case SENSOR_DATA -> exportBucket(
                            createFilePath(influxExportFolder, SENSOR_DATA_FILENAME_PREFIX, timestamp),
                            () -> sensorDataExportService.exportData(Optional.empty(), Optional.empty()).stream(),
                            "sensor data"
                    );
                    case SENSOR_DATA_AGGREGATED -> exportBucket(
                            createFilePath(influxExportFolder, SENSOR_DATA_AGGREGATED_FILENAME_PREFIX, timestamp),
                            () -> sensorDataAggregatedExportService.exportData(Optional.empty(), Optional.empty()).stream(),
                            "aggregated sensor data"
                    );
                    case COSTS -> exportBucket(
                            createFilePath(influxExportFolder, COSTS_FILENAME_PREFIX, timestamp),
                            () -> costsExportService.exportData(Optional.empty(), Optional.empty()).stream(),
                            "costs"
                    );
                };

                if (filePath != null) {
                    exportedFiles.add(filePath);
                }
            } catch (Exception e) {
                LOGGER.error("Failed to export bucket: {}", bucket, e);
                // Continue with other buckets instead of failing completely
            }
        }

        LOGGER.info("Successfully exported {} InfluxDB bucket files", exportedFiles.size());
        return exportedFiles.stream()
                .map(Path::getFileName)
                .toList();
    }

    /**
     * Exports a single bucket with time range parameters.
     *
     * @param bucket The bucket to export
     * @param startTime Optional start time (defaults to 24 hours ago)
     * @param endTime Optional end time (defaults to now)
     * @return Path to the generated file
     */
    public Path exportBucketWithTimeRange(InfluxBucket bucket, Optional<Instant> startTime, Optional<Instant> endTime) {
        LOGGER.info("Starting export of bucket {} with custom time range", bucket);

        final String timestamp = LocalDateTime.now().format(FILE_DATE_TIME_FORMATTER);
        final String influxExportFolder = exportConfig.getCostExportFolder();

        String filenamePrefix = switch (bucket) {
            case SYSTEM_METRICS -> SYSTEM_METRICS_FILENAME_PREFIX;
            case SENSOR_DATA -> SENSOR_DATA_FILENAME_PREFIX;
            case SENSOR_DATA_AGGREGATED -> SENSOR_DATA_AGGREGATED_FILENAME_PREFIX;
            case COSTS -> COSTS_FILENAME_PREFIX;
        };

        return switch (bucket) {
            case SYSTEM_METRICS -> exportBucket(
                    createFilePath(influxExportFolder, filenamePrefix, timestamp),
                    () -> systemMetricsExportService.exportData(startTime, endTime).stream(),
                    "system metrics"
            );
            case SENSOR_DATA -> exportBucket(
                    createFilePath(influxExportFolder, filenamePrefix, timestamp),
                    () -> sensorDataExportService.exportData(startTime, endTime).stream(),
                    "sensor data"
            );
            case SENSOR_DATA_AGGREGATED -> exportBucket(
                    createFilePath(influxExportFolder, filenamePrefix, timestamp),
                    () -> sensorDataAggregatedExportService.exportData(startTime, endTime).stream(),
                    "aggregated sensor data"
            );
            case COSTS -> exportBucket(
                    createFilePath(influxExportFolder, filenamePrefix, timestamp),
                    () -> costsExportService.exportData(startTime, endTime).stream(),
                    "costs"
            );
        };
    }

    private Path createFilePath(String folder, String prefix, String timestamp) {
        return Path.of(folder, prefix + timestamp + FILE_EXTENSION);
    }

    private <T> Path exportBucket(Path path, Supplier<Stream<T>> dataSupplier, String description) {
        LOGGER.info("Exporting {} to file: {}", description, path);

        try {
            Stream<T> dataStream = dataSupplier.get();
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
    public enum InfluxBucket {
        SYSTEM_METRICS("system_metrics", "System performance metrics (CPU, memory, disk, network)"),
        SENSOR_DATA("sensor_data", "Real-time IoT sensor data"),
        SENSOR_DATA_AGGREGATED("sensor_data_30m", "30-minute aggregated sensor data"),
        COSTS("costs", "Cost-related metrics");

        private final String bucketName;
        private final String description;

        InfluxBucket(String bucketName, String description) {
            this.bucketName = bucketName;
            this.description = description;
        }

        public String getBucketName() {
            return bucketName;
        }

        public String getDescription() {
            return description;
        }
    }
}