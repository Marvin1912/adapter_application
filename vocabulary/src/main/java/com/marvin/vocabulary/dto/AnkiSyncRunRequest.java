package com.marvin.vocabulary.dto;

public record AnkiSyncRunRequest(
        String status,
        Long durationMs,
        Integer cardsChanged,
        String errorMessage
) {

}
