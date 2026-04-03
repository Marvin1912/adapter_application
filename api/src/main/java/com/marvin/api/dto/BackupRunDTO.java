package com.marvin.api.dto;

import java.time.LocalDateTime;

/**
 * DTO representing a backup upload run.
 *
 * @param id            the unique identifier of the backup run
 * @param fileName      the name of the backup file
 * @param status        the status of the run (SUCCESS, FAILED, IN_PROGRESS)
 * @param startedAt     the timestamp when the run started
 * @param finishedAt    the timestamp when the run finished
 * @param durationMs    the duration of the run in milliseconds
 * @param uploadSuccess whether the upload was successful
 * @param errorMessage  the error message if the run failed
 */
public record BackupRunDTO(
        Long id,
        String fileName,
        String status,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        Long durationMs,
        Boolean uploadSuccess,
        String errorMessage
) { }
