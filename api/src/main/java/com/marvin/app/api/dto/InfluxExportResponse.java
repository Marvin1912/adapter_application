package com.marvin.app.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class InfluxExportResponse {

    private boolean success;

    private String message;

    private List<String> exportedFiles;

    private Instant timestamp;

    private String error;

    private InfluxExportResponse(boolean success, String message, List<String> exportedFiles, String error) {
        this.success = success;
        this.message = message;
        this.exportedFiles = exportedFiles;
        this.error = error;
        this.timestamp = Instant.now();
    }

    public static InfluxExportResponse success(String message, List<Path> exportedFiles) {
        final List<String> fileNames = exportedFiles.stream()
                .map(path -> path.getFileName().toString())
                .toList();
        return new InfluxExportResponse(true, message, fileNames, null);
    }

    public static InfluxExportResponse error(String errorMessage) {
        return new InfluxExportResponse(false, null, null, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getExportedFiles() {
        return exportedFiles;
    }

    public void setExportedFiles(List<String> exportedFiles) {
        this.exportedFiles = exportedFiles;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
