package com.marvin.vocabulary.dto;

import java.util.List;

public record DictionaryEntry(
        String word,
        String phonetic,
        List<Phonetic> phonetics,
        List<Meaning> meanings,
        License license,
        List<String> sourceUrls
) {

}
