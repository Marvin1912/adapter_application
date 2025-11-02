package com.marvin.vocabulary.dto;

import java.util.List;

public record Meaning(
    String partOfSpeech,
    List<Word> definitions,
    List<String> synonyms,
    List<String> antonyms
) {

}

