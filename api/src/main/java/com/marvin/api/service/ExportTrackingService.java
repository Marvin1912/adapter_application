package com.marvin.api.service;

import com.marvin.database.repository.ExportRunRepository;
import com.marvin.entities.exports.ExportRunEntity;
import com.marvin.upload.Uploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExportTrackingService {

    public enum ExporterType {
        COSTS, VOCABULARY, INFLUXDB
    }

    public enum Status {
        SUCCESS, FAILED
    }

    private final ExportRunRepository exportRunRepository;
    private final Uploader uploader;

    @Transactional
    public List<Path> trackExport(ExporterType exporterType, String exportName, String requestParams, Supplier<List<Path>> exporterCall) {
        ExportRunEntity exportRun = new ExportRunEntity();
        exportRun.setExporterType(exporterType.name());
        exportRun.setExportName(exportName);
        exportRun.setStatus(Status.FAILED.name()); // default to failed, update on success
        exportRun.setStartedAt(LocalDateTime.now());
        exportRun.setRequestParams(requestParams);

        exportRun = exportRunRepository.save(exportRun);

        List<Path> exportedFiles = null;
        try {
            exportedFiles = exporterCall.get();

            // Record exported files as JSON or comma-separated
            if (exportedFiles != null && !exportedFiles.isEmpty()) {
                exportRun.setExportedFiles(exportedFiles.stream()
                        .map(Path::toString)
                        .reduce((a, b) -> a + "," + b)
                        .orElse(""));
            }

            // Call uploader
            boolean uploadSuccess = false;
            try {
                uploader.zipAndUploadFiles(exporterType.name().toLowerCase(), exportedFiles);
                uploadSuccess = true;
            } catch (Exception e) {
                log.error("Upload failed for export run {}", exportRun.getId(), e);
            }

            exportRun.setUploadSuccess(uploadSuccess);
            exportRun.setStatus(Status.SUCCESS.name());
            exportRun.setFinishedAt(LocalDateTime.now());
            exportRun.setDurationMs(java.time.Duration.between(exportRun.getStartedAt(), exportRun.getFinishedAt()).toMillis());

        } catch (Exception e) {
            log.error("Export failed for run {}", exportRun.getId(), e);
            exportRun.setErrorMessage(e.getMessage());
            exportRun.setFinishedAt(LocalDateTime.now());
            if (exportRun.getStartedAt() != null && exportRun.getFinishedAt() != null) {
                exportRun.setDurationMs(java.time.Duration.between(exportRun.getStartedAt(), exportRun.getFinishedAt()).toMillis());
            }
            throw e; // rethrow so controller can handle
        }

        exportRunRepository.save(exportRun);
        return exportedFiles;
    }
}