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
    void getWord_WhenWordNotFoundException_ShouldReturn404WithStructuredError() {
        String word = "nonexistentword";
        WordNotFoundException exception = new WordNotFoundException(word);

        when(dictionaryClient.getWord(word)).thenReturn(Mono.error(exception));

        webTestClient.get()
                .uri("/vocabulary/words/" + word)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(Map.class)
                .value(response -> {
                    assert response.get("type").equals("WORD_NOT_FOUND");
                    assert response.get("message").toString()
                            .contains("The word '" + word + "' was not found");
                    assert response.get("word").equals(word);
                });
    }

    @Test
    void getWord_WhenInvalidWordException_ShouldReturn400WithStructuredError() {
        String word = "invalid123";
        InvalidWordException exception = new InvalidWordException(word);

        when(dictionaryClient.getWord(word)).thenReturn(Mono.error(exception));

        webTestClient.get()
                .uri("/vocabulary/words/" + word)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(Map.class)
                .value(response -> {
                    assert response.get("type").equals("INVALID_WORD");
                    assert response.get("message").toString()
                            .contains("The word '" + word + "' is invalid");
                    assert response.get("word").equals(word);
                });
    }

    @Test
    void getWord_WhenRateLimitExceededException_ShouldReturn429WithStructuredError() {
        String word = "test";
        RateLimitExceededException exception = new RateLimitExceededException();

        when(dictionaryClient.getWord(word)).thenReturn(Mono.error(exception));

        webTestClient.get()
                .uri("/vocabulary/words/" + word)
                .exchange()
                .expectStatus().isEqualTo(429)
                .expectBody(Map.class)
                .value(response -> {
                    assert response.get("type").equals("RATE_LIMIT_EXCEEDED");
                    assert response.get("message").toString().contains("API rate limit exceeded");
                });
    }

    @Test
    void getWord_WhenDictionaryServiceUnavailableException_ShouldReturn503WithStructuredError() {
        String word = "test";
        DictionaryServiceUnavailableException exception = new DictionaryServiceUnavailableException();

        when(dictionaryClient.getWord(word)).thenReturn(Mono.error(exception));

        webTestClient.get()
                .uri("/vocabulary/words/" + word)
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectBody(Map.class)
                .value(response -> {
                    assert response.get("type").equals("SERVICE_UNAVAILABLE");
                    assert response.get("message").toString()
                            .contains("Dictionary API service is currently unavailable");
                });
    }

    @Test
    void getWord_WhenGenericDictionaryApiException_ShouldReturnCorrectStatusCodeWithStructuredError() {
        String word = "test";
        DictionaryApiException exception = new DictionaryApiException(
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
                    assert response.get("type").equals("PAYMENT_REQUIRED");
                    assert response.get("message").toString().contains("Some API error occurred");
                    assert response.get("statusCode").equals("402");
                });
    }

    @Test
    void getWord_WhenRuntimeException_ShouldReturn500WithStructuredError() {
        String word = "test";
        RuntimeException exception = new RuntimeException("Unexpected error");

        when(dictionaryClient.getWord(word)).thenReturn(Mono.error(exception));

        webTestClient.get()
                .uri("/vocabulary/words/" + word)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(Map.class)
                .value(response -> {
                    assert response.get("type").equals("RuntimeException");
                    assert response.get("message").equals("Unexpected error");
                });
    }

    @Test
    void getWord_WhenCustomRateLimitMessage_ShouldReturnCustomMessage() {
        String word = "test";
        String customMessage = "Rate limit: 100 requests per hour exceeded. Try again later.";
        RateLimitExceededException exception = new RateLimitExceededException(customMessage);

        when(dictionaryClient.getWord(word)).thenReturn(Mono.error(exception));

        webTestClient.get()
                .uri("/vocabulary/words/" + word)
                .exchange()
                .expectStatus().isEqualTo(429)
                .expectBody(Map.class)
                .value(response -> {
                    assert response.get("type").equals("RATE_LIMIT_EXCEEDED");
                    assert response.get("message").equals(customMessage);
                });
    }

    @Test
    void getWord_WhenCustomServiceUnavailableMessage_ShouldReturnCustomMessage() {
        String word = "test";
        String customMessage = "Dictionary API is under maintenance. Please try again in 5 minutes.";
        DictionaryServiceUnavailableException exception = new DictionaryServiceUnavailableException(
                customMessage);

        when(dictionaryClient.getWord(word)).thenReturn(Mono.error(exception));

        webTestClient.get()
                .uri("/vocabulary/words/" + word)
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectBody(Map.class)
                .value(response -> {
                    assert response.get("type").equals("SERVICE_UNAVAILABLE");
                    assert response.get("message").equals(customMessage);
                });
    }


    @Test
    void verifyExceptionProperties_AreCorrectlyMapped() {
        String word = "testword";

        // Test WordNotFoundException properties
        WordNotFoundException wordNotFound = new WordNotFoundException(word);
        assert wordNotFound.getWord().equals(word);
        assert wordNotFound.getStatusCode() == 404;
        assert wordNotFound.getErrorType().equals("WORD_NOT_FOUND");

        // Test InvalidWordException properties
        InvalidWordException invalidWord = new InvalidWordException(word);
        assert invalidWord.getWord().equals(word);
        assert invalidWord.getStatusCode() == 400;
        assert invalidWord.getErrorType().equals("INVALID_WORD");

        // Test RateLimitExceededException properties
        RateLimitExceededException rateLimit = new RateLimitExceededException();
        assert rateLimit.getStatusCode() == 429;
        assert rateLimit.getErrorType().equals("RATE_LIMIT_EXCEEDED");

        // Test DictionaryServiceUnavailableException properties
        DictionaryServiceUnavailableException serviceUnavailable = new DictionaryServiceUnavailableException();
        assert serviceUnavailable.getStatusCode() == 503;
        assert serviceUnavailable.getErrorType().equals("SERVICE_UNAVAILABLE");
    }
}
