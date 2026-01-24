package com.marvin.api.controller;

import com.marvin.api.dto.ExportRunDTO;
import com.marvin.database.repository.ExportRunRepository;
import com.marvin.entities.exports.ExportRunEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/exports")
@RequiredArgsConstructor
@Tag(name = "Export Tracking", description = "API for managing and retrieving export run information")
public class ExportTrackingController {

    private final ExportRunRepository exportRunRepository;

    @Operation(
        summary = "Get export runs",
        description = "Retrieves a paginated list of export runs with optional filtering by date range, type, and status"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved export runs",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = Page.class)
            )
        )
    })
    @GetMapping
    public ResponseEntity<Page<ExportRunDTO>> getExportRuns(
            @Parameter(description = "Start date filter (ISO date-time format)", required = false)
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @Parameter(description = "End date filter (ISO date-time format)", required = false)
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @Parameter(description = "Exporter type filter", required = false)
            @RequestParam(required = false) String type,
            @Parameter(description = "Status filter", required = false)
            @RequestParam(required = false) String status,
            @Parameter(description = "Page size (default 20)", required = false)
            @RequestParam(defaultValue = "20") int limit,
            @Parameter(description = "Page offset (default 0)", required = false)
            @RequestParam(defaultValue = "0") int offset) {

        final Pageable pageable = PageRequest.of(offset / limit, limit);
        final Page<ExportRunEntity> entities = exportRunRepository.findByFilters(from, to, type, status, pageable);
        final Page<ExportRunDTO> dtos = entities.map(this::toDTO);

        return ResponseEntity.ok(dtos);
    }

    @Operation(
        summary = "Get export run by ID",
        description = "Retrieves details of a specific export run"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved export run",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ExportRunDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Export run not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ExportRunDTO> getExportRun(
            @Parameter(description = "Export run ID", required = true)
            @PathVariable Long id) {
        final Optional<ExportRunEntity> entity = exportRunRepository.findById(id);
        if (entity.isPresent()) {
            return ResponseEntity.ok(toDTO(entity.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private ExportRunDTO toDTO(ExportRunEntity entity) {
        List<String> exportedFiles = null;
        if (entity.getExportedFiles() != null && !entity.getExportedFiles().isEmpty()) {
            exportedFiles = Arrays.asList(entity.getExportedFiles().split(","));
        }

        return new ExportRunDTO(
                entity.getId(),
                entity.getExporterType(),
                entity.getExportName(),
                entity.getStatus(),
                entity.getStartedAt(),
                entity.getFinishedAt(),
                entity.getDurationMs(),
                exportedFiles,
                entity.getUploadSuccess(),
                entity.getErrorMessage(),
                entity.getRequestParams()
        );
    }
}