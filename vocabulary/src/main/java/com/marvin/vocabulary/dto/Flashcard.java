package com.marvin.vocabulary.dto;

public record Flashcard(
        Integer id,
        Integer deckId,
        String ankiId,
        String front,
        String back,
        String description,
        boolean updated
) {

}
