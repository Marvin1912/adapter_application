package com.marvin.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExportRunDTO {
    private Long id;
    private String exporterType;
    private String exportName;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private Long durationMs;
    private List<String> exportedFiles;
    private Boolean uploadSuccess;
    private String errorMessage;
    private String requestParams;
}