package com.marvin.app.controller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileDeleteResponse {

    private boolean success;

    private String message;

    private String fileId;

    private Instant timestamp;

    private String error;

    private FileDeleteResponse(boolean success, String message, String fileId, String error) {
        this.success = success;
        this.message = message;
        this.fileId = fileId;
        this.error = error;
        this.timestamp = Instant.now();
    }

    public static FileDeleteResponse success(String message, String fileId) {
        return new FileDeleteResponse(true, message, fileId, null);
    }

    public static FileDeleteResponse error(String errorMessage) {
        return new FileDeleteResponse(false, null, null, errorMessage);
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

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
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