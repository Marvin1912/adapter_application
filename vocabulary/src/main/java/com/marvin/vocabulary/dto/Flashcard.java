package com.marvin.vocabulary.dto;

public record Flashcard(
    Integer id,
    String deck,
    String ankiId,
    String front,
    String back,
    String description,
    boolean updated
) {

}
