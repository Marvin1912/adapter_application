package com.marvin.backup.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** JPA entity representing a single backup upload run in the exports schema. */
@Getter
@Setter
@Entity
@Table(name = "backup_run", schema = "exports")
public class BackupRunEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    private String status;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    private Long durationMs;

    private Boolean uploadSuccess;

    private String errorMessage;

}
