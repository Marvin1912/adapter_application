package com.marvin.vocabulary.exceptions;

public class DictionaryServiceUnavailableException extends DictionaryApiException {

    public DictionaryServiceUnavailableException() {
        super("Dictionary API service is currently unavailable. Please try again later.", 503,
                "SERVICE_UNAVAILABLE");
    }

    public DictionaryServiceUnavailableException(String message) {
        super(message, 503, "SERVICE_UNAVAILABLE");
    }
}
