package com.marvin.vocabulary.dictionaryapi;

import com.marvin.vocabulary.dto.DictionaryEntry;
import com.marvin.vocabulary.exceptions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

class DictionaryClientTest {

    private DictionaryClient dictionaryClient;

    @BeforeEach
    void setUp() {
        dictionaryClient = new DictionaryClient();
    }

    @Test
    void getWord_WhenNullWord_ShouldReturnInvalidWordException() {
        String nullWord = null;

        Mono<List<DictionaryEntry>> result = dictionaryClient.getWord(nullWord);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                    throwable instanceof InvalidWordException &&
                    throwable.getMessage().contains("The word 'null' is invalid or contains unsupported characters")
                )
                .verify();
    }

    @Test
    void getWord_WhenEmptyWord_ShouldReturnInvalidWordException() {
        String emptyWord = "";

        Mono<List<DictionaryEntry>> result = dictionaryClient.getWord(emptyWord);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                    throwable instanceof InvalidWordException &&
                    throwable.getMessage().contains("The word '' is invalid or contains unsupported characters")
                )
                .verify();
    }

    @Test
    void getWord_WhenBlankWord_ShouldReturnInvalidWordException() {
        String blankWord = "   ";

        Mono<List<DictionaryEntry>> result = dictionaryClient.getWord(blankWord);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                    throwable instanceof InvalidWordException &&
                    throwable.getMessage().contains("The word '   ' is invalid or contains unsupported characters")
                )
                .verify();
    }

    @Test
    void getWord_WhenWordWithNumbers_ShouldReturnInvalidWordException() {
        String wordWithNumbers = "hello123";

        Mono<List<DictionaryEntry>> result = dictionaryClient.getWord(wordWithNumbers);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                    throwable instanceof InvalidWordException &&
                    throwable.getMessage().contains("The word 'hello123' is invalid or contains unsupported characters")
                )
                .verify();
    }

    @Test
    void getWord_WhenWordWithSpecialCharacters_ShouldReturnInvalidWordException() {
        String wordWithSpecialChars = "hello!@#";

        Mono<List<DictionaryEntry>> result = dictionaryClient.getWord(wordWithSpecialChars);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                    throwable instanceof InvalidWordException &&
                    throwable.getMessage().contains("The word 'hello!@#' is invalid or contains unsupported characters")
                )
                .verify();
    }

    
    @Test
    void invalidWordException_ShouldContainCorrectProperties() {
        String invalidWord = "test123";

        Mono<List<DictionaryEntry>> result = dictionaryClient.getWord(invalidWord);

        StepVerifier.create(result)
                .consumeErrorWith(throwable -> {
                    InvalidWordException exception = (InvalidWordException) throwable;
                    assert exception.getWord().equals(invalidWord);
                    assert exception.getStatusCode() == 400;
                    assert exception.getErrorType().equals("INVALID_WORD");
                })
                .verify();
    }

    @Test
    void validation_ShouldAcceptValidWordPatterns() {
        // Test that valid patterns don't immediately throw validation errors
        // Note: These might still fail at the API call level, but not due to validation
        String[] validWords = {
            "hello",
            "world",
            "test"
        };

        for (String word : validWords) {
            try {
                dictionaryClient.getWord(word);
                // If we get here without exception, validation passed
            } catch (InvalidWordException e) {
                // This should not happen for valid words
                assert false : "Valid word '" + word + "' should not throw InvalidWordException";
            }
        }
    }

    @Test
    void validation_ShouldRejectInvalidWordPatterns() {
        String[] invalidWords = {
            "hello123",
            "test@word",
            "word#hash",
            "123456",
            "hello!",
            "test.word",
            "word$sign",
            "",
            "   ",
            null,
            "word@domain.com",
            "test&case",
            "hello%world"
        };

        for (String word : invalidWords) {
            Mono<List<DictionaryEntry>> result = dictionaryClient.getWord(word);

            // These should all fail validation with InvalidWordException
            StepVerifier.create(result)
                    .expectError(InvalidWordException.class)
                    .verify();
        }
    }
}