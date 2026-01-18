package com.marvin.entities.exports;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "export_run", schema = "exports")
public class ExportRunEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Long id;

    @Basic
    @Column(name = "exporter_type", nullable = false, length = 64)
    private String exporterType;

    @Basic
    @Column(name = "export_name", length = 128)
    private String exportName;

    @Basic
    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Basic
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Basic
    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Basic
    @Column(name = "duration_ms")
    private Long durationMs;

    @Basic
    @Column(name = "exported_files")
    private String exportedFiles;

    @Basic
    @Column(name = "upload_success")
    private Boolean uploadSuccess;

    @Basic
    @Column(name = "error_message")
    private String errorMessage;

    @Basic
    @Column(name = "request_params")
    private String requestParams;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExporterType() {
        return exporterType;
    }

    public void setExporterType(String exporterType) {
        this.exporterType = exporterType;
    }

    public String getExportName() {
        return exportName;
    }

    public void setExportName(String exportName) {
        this.exportName = exportName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public String getExportedFiles() {
        return exportedFiles;
    }

    public void setExportedFiles(String exportedFiles) {
        this.exportedFiles = exportedFiles;
    }

    public Boolean getUploadSuccess() {
        return uploadSuccess;
    }

    public void setUploadSuccess(Boolean uploadSuccess) {
        this.uploadSuccess = uploadSuccess;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(String requestParams) {
        this.requestParams = requestParams;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExportRunEntity that = (ExportRunEntity) o;
        return Objects.equals(id, that.id)
                && Objects.equals(exporterType, that.exporterType)
                && Objects.equals(exportName, that.exportName)
                && Objects.equals(status, that.status)
                && Objects.equals(startedAt, that.startedAt)
                && Objects.equals(finishedAt, that.finishedAt)
                && Objects.equals(durationMs, that.durationMs)
                && Objects.equals(exportedFiles, that.exportedFiles)
                && Objects.equals(uploadSuccess, that.uploadSuccess)
                && Objects.equals(errorMessage, that.errorMessage)
                && Objects.equals(requestParams, that.requestParams);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, exporterType, exportName, status, startedAt, finishedAt, durationMs,
                exportedFiles, uploadSuccess, errorMessage, requestParams);
    }

    @Override
    public String toString() {
        return "ExportRunEntity{"
                + "id=" + id
                + ", exporterType='" + exporterType + '\''
                + ", exportName='" + exportName + '\''
                + ", status='" + status + '\''
                + ", startedAt=" + startedAt
                + ", finishedAt=" + finishedAt
                + ", durationMs=" + durationMs
                + ", exportedFiles='" + exportedFiles + '\''
                + ", uploadSuccess=" + uploadSuccess
                + ", errorMessage='" + errorMessage + '\''
                + ", requestParams='" + requestParams + '\''
                + '}';
    }
}
