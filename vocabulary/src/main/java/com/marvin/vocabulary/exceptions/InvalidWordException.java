package com.marvin.vocabulary.exceptions;

public class InvalidWordException extends DictionaryApiException {

  private final String word;

  public InvalidWordException(String word) {
    super(String.format("The word '%s' is invalid or contains unsupported characters.", word),
        400, "INVALID_WORD");
    this.word = word;
  }

  public String getWord() {
    return word;
  }
}
