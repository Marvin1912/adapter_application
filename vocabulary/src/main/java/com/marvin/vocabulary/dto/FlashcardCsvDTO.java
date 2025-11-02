package com.marvin.vocabulary.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FlashcardCsvDTO(
    @JsonProperty("deck")
    String deck,
    @JsonProperty("guid")
    String guid,
    @JsonProperty("front")
    String front,
    @JsonProperty("back")
    String back,
    @JsonProperty("description")
    String description
) {

}
