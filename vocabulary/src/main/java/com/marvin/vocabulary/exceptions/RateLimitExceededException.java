package com.marvin.vocabulary.exceptions;

public class RateLimitExceededException extends DictionaryApiException {

  public RateLimitExceededException() {
    super("API rate limit exceeded. Please try again later.", 429, "RATE_LIMIT_EXCEEDED");
  }

  public RateLimitExceededException(String message) {
    super(message, 429, "RATE_LIMIT_EXCEEDED");
  }
}
