package com.marvin.vocabulary.controller;

import static org.mockito.Mockito.when;

import com.marvin.vocabulary.dictionaryapi.DictionaryClient;
import com.marvin.vocabulary.exceptions.DictionaryApiException;
import com.marvin.vocabulary.exceptions.DictionaryServiceUnavailableException;
import com.marvin.vocabulary.exceptions.InvalidWordException;
import com.marvin.vocabulary.exceptions.RateLimitExceededException;
import com.marvin.vocabulary.exceptions.WordNotFoundException;
import com.marvin.vocabulary.service.FlashcardService;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class FlashcardControllerErrorHandlingTest {

  @Mock
  private DictionaryClient dictionaryClient;

  @Mock
  private FlashcardService flashcardService;

  @InjectMocks
  private FlashcardController flashcardController;

  private WebTestClient webTestClient;

  @BeforeEach
  void setUp() {
    webTestClient = WebTestClient.bindToController(flashcardController).build();
  }

  @Test
  void getWordWhenWordNotFoundExceptionShouldReturn404WithStructuredError() {
    final String word = "nonexistentword";
    final WordNotFoundException exception = new WordNotFoundException(word);

    when(dictionaryClient.getWord(word)).thenReturn(Mono.error(exception));

    webTestClient.get()
        .uri("/vocabulary/words/" + word)
        .exchange()
        .expectStatus().isNotFound()
        .expectBody(Map.class)
        .value(response -> {
          assert "WORD_NOT_FOUND".equals(response.get("type"));
          assert response.get("message").toString()
              .contains("The word '" + word + "' was not found");
          assert response.get("word").equals(word);
        });
  }

  @Test
  void getWordWhenInvalidWordExceptionShouldReturn400WithStructuredError() {
    final String word = "invalid123";
    final InvalidWordException exception = new InvalidWordException(word);

    when(dictionaryClient.getWord(word)).thenReturn(Mono.error(exception));

    webTestClient.get()
        .uri("/vocabulary/words/" + word)
        .exchange()
        .expectStatus().isBadRequest()
        .expectBody(Map.class)
        .value(response -> {
          assert "INVALID_WORD".equals(response.get("type"));
          assert response.get("message").toString()
              .contains("The word '" + word + "' is invalid");
          assert response.get("word").equals(word);
        });
  }

  @Test
  void getWordWhenRateLimitExceededExceptionShouldReturn429WithStructuredError() {
    final String word = "test";
    final RateLimitExceededException exception = new RateLimitExceededException();

    when(dictionaryClient.getWord(word)).thenReturn(Mono.error(exception));

    webTestClient.get()
        .uri("/vocabulary/words/" + word)
        .exchange()
        .expectStatus().isEqualTo(429)
        .expectBody(Map.class)
        .value(response -> {
          assert "RATE_LIMIT_EXCEEDED".equals(response.get("type"));
          assert response.get("message").toString().contains("API rate limit exceeded");
        });
  }

  @Test
  void getWordWhenDictionaryServiceUnavailableExceptionShouldReturn503WithStructuredError() {
    final String word = "test";
    final DictionaryServiceUnavailableException exception = new DictionaryServiceUnavailableException();

    when(dictionaryClient.getWord(word)).thenReturn(Mono.error(exception));

    webTestClient.get()
        .uri("/vocabulary/words/" + word)
        .exchange()
        .expectStatus().isEqualTo(503)
        .expectBody(Map.class)
        .value(response -> {
          assert "SERVICE_UNAVAILABLE".equals(response.get("type"));
          assert response.get("message").toString()
              .contains("Dictionary API service is currently unavailable");
        });
  }

  @Test
  void getWordWhenGenericDictionaryApiExceptionShouldReturnCorrectStatusCodeWithStructuredError() {
    final String word = "test";
    final DictionaryApiException exception = new DictionaryApiException(
        "Some API error occurred",
        402,
        "PAYMENT_REQUIRED"
    );

    when(dictionaryClient.getWord(word)).thenReturn(Mono.error(exception));

    webTestClient.get()
        .uri("/vocabulary/words/" + word)
        .exchange()
        .expectStatus().isEqualTo(402)
        .expectBody(Map.class)
        .value(response -> {
          assert "PAYMENT_REQUIRED".equals(response.get("type"));
          assert response.get("message").toString().contains("Some API error occurred");
          assert "402".equals(response.get("statusCode"));
        });
  }

  @Test
  void getWordWhenRuntimeExceptionShouldReturn500WithStructuredError() {
    final String word = "test";
    final RuntimeException exception = new RuntimeException("Unexpected error");

    when(dictionaryClient.getWord(word)).thenReturn(Mono.error(exception));

    webTestClient.get()
        .uri("/vocabulary/words/" + word)
        .exchange()
        .expectStatus().is5xxServerError()
        .expectBody(Map.class)
        .value(response -> {
          assert "RuntimeException".equals(response.get("type"));
          assert "Unexpected error".equals(response.get("message"));
        });
  }

  @Test
  void getWordWhenCustomRateLimitMessageShouldReturnCustomMessage() {
    final String word = "test";
    final String customMessage = "Rate limit: 100 requests per hour exceeded. Try again later.";
    final RateLimitExceededException exception = new RateLimitExceededException(customMessage);

    when(dictionaryClient.getWord(word)).thenReturn(Mono.error(exception));

    webTestClient.get()
        .uri("/vocabulary/words/" + word)
        .exchange()
        .expectStatus().isEqualTo(429)
        .expectBody(Map.class)
        .value(response -> {
          assert "RATE_LIMIT_EXCEEDED".equals(response.get("type"));
          assert response.get("message").equals(customMessage);
        });
  }

  @Test
  void getWordWhenCustomServiceUnavailableMessageShouldReturnCustomMessage() {
    final String word = "test";
    final String customMessage =
        "Dictionary API is under maintenance. Please try again in 5 minutes.";
    final DictionaryServiceUnavailableException exception =
        new DictionaryServiceUnavailableException(customMessage);

    when(dictionaryClient.getWord(word)).thenReturn(Mono.error(exception));

    webTestClient.get()
        .uri("/vocabulary/words/" + word)
        .exchange()
        .expectStatus().isEqualTo(503)
        .expectBody(Map.class)
        .value(response -> {
          assert "SERVICE_UNAVAILABLE".equals(response.get("type"));
          assert response.get("message").equals(customMessage);
        });
  }


  @Test
  void verifyExceptionPropertiesAreCorrectlyMapped() {
    final String word = "testword";

    // Test WordNotFoundException properties
    final WordNotFoundException wordNotFound = new WordNotFoundException(word);
    assert wordNotFound.getWord().equals(word);
    assert wordNotFound.getStatusCode() == 404;
    assert "WORD_NOT_FOUND".equals(wordNotFound.getErrorType());

    // Test InvalidWordException properties
    final InvalidWordException invalidWord = new InvalidWordException(word);
    assert invalidWord.getWord().equals(word);
    assert invalidWord.getStatusCode() == 400;
    assert "INVALID_WORD".equals(invalidWord.getErrorType());

    // Test RateLimitExceededException properties
    final RateLimitExceededException rateLimit = new RateLimitExceededException();
    assert rateLimit.getStatusCode() == 429;
    assert "RATE_LIMIT_EXCEEDED".equals(rateLimit.getErrorType());

    // Test DictionaryServiceUnavailableException properties
    final DictionaryServiceUnavailableException serviceUnavailable =
        new DictionaryServiceUnavailableException();
    assert serviceUnavailable.getStatusCode() == 503;
    assert "SERVICE_UNAVAILABLE".equals(serviceUnavailable.getErrorType());
  }
}
