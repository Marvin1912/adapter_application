package com.marvin.vocabulary.exceptions;

public class WordNotFoundException extends DictionaryApiException {

    private final String word;

    public WordNotFoundException(String word) {
        super(String.format("The word '%s' was not found in the dictionary.", word), 404,
                "WORD_NOT_FOUND");
        this.word = word;
    }

    public String getWord() {
        return word;
    }
}
