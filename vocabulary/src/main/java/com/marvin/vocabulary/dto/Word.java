package com.marvin.vocabulary.dto;

import java.util.List;

public record Word(
    String definition,
    String example,
    List<String> synonyms,
    List<String> antonyms
) {

}

