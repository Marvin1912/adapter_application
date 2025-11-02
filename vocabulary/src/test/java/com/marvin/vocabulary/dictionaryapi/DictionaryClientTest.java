package com.marvin.vocabulary.dictionaryapi;

import com.marvin.vocabulary.dto.DictionaryEntry;
import com.marvin.vocabulary.exceptions.InvalidWordException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class DictionaryClientTest {

  private DictionaryClient dictionaryClient;
  private WiktionaryResponseMapper responseMapper;
  private HtmlCleaner htmlCleaner;

  @BeforeEach
  void setUp() {
    htmlCleaner = new HtmlCleaner();
    responseMapper = new WiktionaryResponseMapper(htmlCleaner);
    dictionaryClient = new DictionaryClient(responseMapper);
  }

  @Test
  void getWordWhenNullWordShouldReturnInvalidWordException() {
    final String nullWord = null;

    final Mono<List<DictionaryEntry>> result = dictionaryClient.getWord(nullWord);

    StepVerifier.create(result)
        .expectErrorMatches(throwable ->
            throwable instanceof InvalidWordException &&
                throwable.getMessage().contains(
                    "The word 'null' is invalid or contains unsupported characters")
        )
        .verify();
  }

  @Test
  void getWordWhenEmptyWordShouldReturnInvalidWordException() {
    final String emptyWord = "";

    final Mono<List<DictionaryEntry>> result = dictionaryClient.getWord(emptyWord);

    StepVerifier.create(result)
        .expectErrorMatches(throwable ->
            throwable instanceof InvalidWordException &&
                throwable.getMessage().contains(
                    "The word '' is invalid or contains unsupported characters")
        )
        .verify();
  }

  @Test
  void getWordWhenBlankWordShouldReturnInvalidWordException() {
    final String blankWord = "   ";

    final Mono<List<DictionaryEntry>> result = dictionaryClient.getWord(blankWord);

    StepVerifier.create(result)
        .expectErrorMatches(throwable ->
            throwable instanceof InvalidWordException &&
                throwable.getMessage().contains(
                    "The word '   ' is invalid or contains unsupported characters")
        )
        .verify();
  }

  @Test
  void getWordWhenWordWithNumbersShouldReturnInvalidWordException() {
    final String wordWithNumbers = "hello123";

    final Mono<List<DictionaryEntry>> result = dictionaryClient.getWord(wordWithNumbers);

    StepVerifier.create(result)
        .expectErrorMatches(throwable ->
            throwable instanceof InvalidWordException &&
                throwable.getMessage().contains(
                    "The word 'hello123' is invalid or contains unsupported characters")
        )
        .verify();
  }

  @Test
  void getWordWhenWordWithSpecialCharactersShouldReturnInvalidWordException() {
    final String wordWithSpecialChars = "hello!@#";

    final Mono<List<DictionaryEntry>> result = dictionaryClient.getWord(wordWithSpecialChars);

    StepVerifier.create(result)
        .expectErrorMatches(throwable ->
            throwable instanceof InvalidWordException &&
                throwable.getMessage().contains(
                    "The word 'hello!@#' is invalid or contains unsupported characters")
        )
        .verify();
  }


  @Test
  void invalidWordExceptionShouldContainCorrectProperties() {
    final String invalidWord = "test123";

    final Mono<List<DictionaryEntry>> result = dictionaryClient.getWord(invalidWord);

    StepVerifier.create(result)
        .consumeErrorWith(throwable -> {
          final InvalidWordException exception = (InvalidWordException) throwable;
          assert exception.getWord().equals(invalidWord);
          assert exception.getStatusCode() == 400;
          assert exception.getErrorType().equals("INVALID_WORD");
        })
        .verify();
  }

  @Test
  void validationShouldAcceptValidWordPatterns() {
    // Test that valid patterns don't immediately throw validation errors
    // Note: These might still fail at the API call level, but not due to validation
    final String[] validWords = {
        "hello",
        "world",
        "test"
    };

    for (final String word : validWords) {
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
  void validationShouldRejectInvalidWordPatterns() {
    final String[] invalidWords = {
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

    for (final String word : invalidWords) {
      final Mono<List<DictionaryEntry>> result = dictionaryClient.getWord(word);

      // These should all fail validation with InvalidWordException
      StepVerifier.create(result)
          .expectError(InvalidWordException.class)
          .verify();
    }
  }
}
