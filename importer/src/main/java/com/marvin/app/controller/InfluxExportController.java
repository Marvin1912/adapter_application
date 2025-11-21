package com.marvin.app.controller;

import com.marvin.app.controller.dto.InfluxBucketResponse;
import com.marvin.app.controller.dto.InfluxExportRequest;
import com.marvin.app.controller.dto.InfluxExportResponse;
import com.marvin.export.InfluxExporter;
import com.marvin.export.InfluxExporter.InfluxBucket;
import com.marvin.upload.Uploader;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "InfluxDB Export", description = "API for exporting InfluxDB bucket data")
public class InfluxExportController {

    private final InfluxExporter influxExporter;
    private final Uploader uploader;

    public InfluxExportController(InfluxExporter influxExporter, Uploader uploader) {
        this.influxExporter = influxExporter;
        this.uploader = uploader;
    }

    @Operation(
        summary = "Get available InfluxDB buckets",
        description = "Retrieves a list of all available InfluxDB buckets that can be exported. Each bucket includes its name, bucket identifier, and description."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved available buckets",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = InfluxBucketResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error occurred while retrieving buckets",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = InfluxBucketResponse.class)
            )
        )
    })
    @GetMapping(path = "/export/influxdb/buckets", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InfluxBucketResponse> getAvailableBuckets() {
        try {
            final List<InfluxBucketResponse.InfluxBucketDTO> buckets = Arrays.stream(InfluxExporter.InfluxBucket.values())
                    .map(bucket -> new InfluxBucketResponse.InfluxBucketDTO(
                            bucket.name(),
                            bucket.getBucketName(),
                            bucket.getDescription()
                    ))
                    .toList();

            return ResponseEntity.ok(InfluxBucketResponse.success(buckets));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(InfluxBucketResponse.error("Failed to retrieve available buckets: " + e.getMessage()));
        }
    }

    @Operation(
        summary = "Export InfluxDB buckets",
        description = "Exports data from selected InfluxDB buckets with optional time range filtering. The export is performed asynchronously and returns information about the generated files. " +
                   "Time range filters use ISO-8601 format: yyyy-MM-dd'T'HH:mm:ss (e.g., 2024-01-15T10:30:00). If startTime is not provided, defaults to 5 years ago; if endTime is not provided, defaults to current time (implemented in AbstractInfluxExport)."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully initiated bucket export",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = InfluxExportResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters or bucket names",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = InfluxExportResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error occurred during export",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = InfluxExportResponse.class)
            )
        )
    })
    @PostMapping(path = "/export/influxdb", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InfluxExportResponse> exportInfluxBuckets(
            @Parameter(
                description = "Export request configuration including bucket selection and optional time range",
                required = true,
                schema = @Schema(implementation = InfluxExportRequest.class)
            )
            @RequestBody InfluxExportRequest request) {

        try {
            final List<Path> exportedFiles;

            final InfluxExporter.InfluxBucket bucketEnum = InfluxBucket.valueOf(request.getBucket());

            final String startTime = request.getStartTime();
            final String endTime = request.getEndTime();
            exportedFiles = influxExporter.exportSelectedBucket(
                    bucketEnum,
                    startTime != null ? ZonedDateTime.parse(startTime).toInstant() : null,
                    endTime != null ? ZonedDateTime.parse(endTime).toInstant() : null
            );

            uploader.zipAndUploadCostFiles(bucketEnum.name(), exportedFiles);

            return ResponseEntity.ok(InfluxExportResponse.success("InfluxDB buckets exported and uploaded successfully", exportedFiles));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(InfluxExportResponse.error("Invalid bucket name: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(InfluxExportResponse.error("Failed to export InfluxDB buckets: " + e.getMessage()));
        }
    }
}
