package com.marvin.vocabulary.dto;

public record Phonetic(
        String text,
        String audio,
        String sourceUrl,
        License license
) {

}

