package com.marvin.entities.exports;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
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
}
