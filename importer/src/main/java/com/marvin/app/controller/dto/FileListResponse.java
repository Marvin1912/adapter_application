package com.marvin.app.controller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.marvin.upload.DriveFileInfo;
import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileListResponse {

    private boolean success;

    private String message;

    private List<DriveFileInfo> files;

    private Instant timestamp;

    private String error;

    private FileListResponse(boolean success, String message, List<DriveFileInfo> files, String error) {
        this.success = success;
        this.message = message;
        this.files = files;
        this.error = error;
        this.timestamp = Instant.now();
    }

    public static FileListResponse success(String message, List<DriveFileInfo> files) {
        return new FileListResponse(true, message, files, null);
    }

    public static FileListResponse error(String errorMessage) {
        return new FileListResponse(false, null, null, errorMessage);
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

    public List<DriveFileInfo> getFiles() {
        return files;
    }

    public void setFiles(List<DriveFileInfo> files) {
        this.files = files;
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