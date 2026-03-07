package com.marvin.api.controller;

import com.marvin.api.dto.BackupRunDTO;
import com.marvin.database.repository.BackupRunRepository;
import com.marvin.entities.exports.BackupRunEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.Optional;
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

@RestController
@RequestMapping("/backups")
@RequiredArgsConstructor
@Tag(name = "Backup Runs", description = "API for retrieving backup upload run history")
public class BackupRunController {

    private final BackupRunRepository backupRunRepository;

    @Operation(
        summary = "Get backup runs",
        description = "Retrieves a paginated list of backup upload runs with optional filtering by date range and status"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved backup runs",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Page.class))
        )
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<BackupRunDTO>> getBackupRuns(
            @Parameter(description = "Start date filter") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @Parameter(description = "End date filter") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @Parameter(description = "Status filter (SUCCESS, FAILED, IN_PROGRESS)") @RequestParam(required = false) String status,
            @Parameter(description = "Page size (default 20)") @RequestParam(defaultValue = "20") int limit,
            @Parameter(description = "Page offset (default 0)") @RequestParam(defaultValue = "0") int offset) {

        final Pageable pageable = PageRequest.of(offset / limit, limit);
        final Page<BackupRunDTO> page = backupRunRepository.findByFilters(from, to, status, pageable)
                .map(this::toDTO);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Get backup run by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved backup run",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BackupRunDTO.class))),
        @ApiResponse(responseCode = "404", description = "Backup run not found")
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BackupRunDTO> getBackupRun(
            @Parameter(description = "Backup run ID", required = true) @PathVariable Long id) {
        final Optional<BackupRunEntity> entity = backupRunRepository.findById(id);
        return entity.map(e -> ResponseEntity.ok(toDTO(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    private BackupRunDTO toDTO(BackupRunEntity entity) {
        return new BackupRunDTO(
                entity.getId(),
                entity.getFileName(),
                entity.getStatus(),
                entity.getStartedAt(),
                entity.getFinishedAt(),
                entity.getDurationMs(),
                entity.getUploadSuccess(),
                entity.getErrorMessage()
        );
    }
}
