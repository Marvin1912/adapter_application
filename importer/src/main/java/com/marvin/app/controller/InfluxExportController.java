package com.marvin.app.controller;

import com.marvin.app.controller.dto.InfluxExportRequest;
import com.marvin.app.controller.dto.InfluxExportResponse;
import com.marvin.export.InfluxExporter;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for triggering InfluxDB data exports. Provides endpoint to export data from user buckets with optional filtering.
 */
@RestController
public class InfluxExportController {

    private final InfluxExporter influxExporter;

    public InfluxExportController(InfluxExporter influxExporter) {
        this.influxExporter = influxExporter;
    }

    /**
     * Export InfluxDB user buckets.
     *
     * @param request Export configuration with bucket selection and optional time range
     * @return Export response with generated file information
     */
    @PostMapping(path = "/export/influxdb", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InfluxExportResponse> exportInfluxBuckets(@RequestBody InfluxExportRequest request) {

        try {
            final List<Path> exportedFiles;

            // Export specific buckets
            final Set<InfluxExporter.InfluxBucket> bucketEnums = request.getBuckets().stream()
                    .map(InfluxExporter.InfluxBucket::valueOf)
                    .collect(Collectors.toSet());

            final String startTime = request.getStartTime();
            final String endTime = request.getEndTime();
            exportedFiles = influxExporter.exportSelectedBuckets(
                    bucketEnums,
                    startTime != null ? ZonedDateTime.parse(startTime).toInstant() : null,
                    endTime != null ? ZonedDateTime.parse(endTime).toInstant() : null
            );

            return ResponseEntity.ok(InfluxExportResponse.success("InfluxDB buckets exported successfully", exportedFiles));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(InfluxExportResponse.error("Invalid bucket name: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(InfluxExportResponse.error("Failed to export InfluxDB buckets: " + e.getMessage()));
        }
    }
}
