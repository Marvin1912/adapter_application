package com.marvin.api.controller;

import com.marvin.api.dto.ExportRunDTO;
import com.marvin.database.repository.ExportRunRepository;
import com.marvin.entities.exports.ExportRunEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
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
public class ExportTrackingController {

    private final ExportRunRepository exportRunRepository;

    @GetMapping
    public ResponseEntity<Page<ExportRunDTO>> getExportRuns(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {

        final Pageable pageable = PageRequest.of(offset / limit, limit);
        final Page<ExportRunEntity> entities = exportRunRepository.findByFilters(from, to, type, status, pageable);
        final Page<ExportRunDTO> dtos = entities.map(this::toDTO);

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExportRunDTO> getExportRun(@PathVariable Long id) {
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