package com.marvin.app.controller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Response DTO for InfluxDB export operations.
 * Provides export status, messages, and generated file information.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InfluxExportResponse {

    /**
     * Indicates whether the export operation was successful.
     */
    private boolean success;

    /**
     * Status message describing the export result.
     */
    private String message;

    /**
     * List of generated export file paths (filenames only).
     */
    private List<String> exportedFiles;

    /**
     * Timestamp when the export was completed.
     */
    private Instant timestamp;

    /**
     * Error details (only present when success is false).
     */
    private String error;

    private InfluxExportResponse(boolean success, String message, List<String> exportedFiles, String error) {
        this.success = success;
        this.message = message;
        this.exportedFiles = exportedFiles;
        this.error = error;
        this.timestamp = Instant.now();
    }

    /**
     * Creates a successful export response.
     *
     * @param message Success message
     * @param exportedFiles List of exported file paths
     * @return Successful response
     */
    public static InfluxExportResponse success(String message, java.util.List<java.nio.file.Path> exportedFiles) {
        List<String> fileNames = exportedFiles.stream()
            .map(path -> path.getFileName().toString())
            .toList();
        return new InfluxExportResponse(true, message, fileNames, null);
    }

    /**
     * Creates an error export response.
     *
     * @param errorMessage Error message
     * @return Error response
     */
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