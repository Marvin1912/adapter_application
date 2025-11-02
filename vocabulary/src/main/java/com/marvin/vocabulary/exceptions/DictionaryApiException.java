package com.marvin.vocabulary.exceptions;

public class DictionaryApiException extends RuntimeException {

  private final int statusCode;
  private final String errorType;

  public DictionaryApiException(String message, int statusCode, String errorType) {
    super(message);
    this.statusCode = statusCode;
    this.errorType = errorType;
  }

  public DictionaryApiException(String message, Throwable cause, int statusCode,
      String errorType) {
    super(message, cause);
    this.statusCode = statusCode;
    this.errorType = errorType;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public String getErrorType() {
    return errorType;
  }
}
