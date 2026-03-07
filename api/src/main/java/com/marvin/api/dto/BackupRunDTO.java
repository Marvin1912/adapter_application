package com.marvin.api.dto;

import java.time.LocalDateTime;

public record BackupRunDTO(
        Long id,
        String fileName,
        String status,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        Long durationMs,
        Boolean uploadSuccess,
        String errorMessage
) {}
