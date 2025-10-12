package com.marvin.vocabulary.dictionaryapi;

import com.marvin.vocabulary.dto.DictionaryEntry;
import com.marvin.vocabulary.exceptions.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class DictionaryClient {

    private static final ParameterizedTypeReference<List<DictionaryEntry>> DICT_REFERENCE =
            new ParameterizedTypeReference<>() {
            };

    private final WebClient webClient;

    public DictionaryClient() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.dictionaryapi.dev/api/v2/entries/en")
                .build();
    }

    public Mono<List<DictionaryEntry>> getWord(String word) {
        if (word == null || word.trim().isEmpty()) {
            return Mono.error(new InvalidWordException(word));
        }

        if (!word.matches("^[a-zA-Z\\s-]+$")) {
            return Mono.error(new InvalidWordException(word));
        }

        return webClient.get()
                .uri("/" + word.trim())
                .retrieve()
                .onStatus(
                    HttpStatus.NOT_FOUND::equals,
                    response -> Mono.error(new WordNotFoundException(word))
                )
                .onStatus(
                    HttpStatus.TOO_MANY_REQUESTS::equals,
                    response -> Mono.error(new RateLimitExceededException())
                )
                .onStatus(
                    HttpStatus.SERVICE_UNAVAILABLE::equals,
                    response -> Mono.error(new DictionaryServiceUnavailableException())
                )
                .onStatus(
                    HttpStatus.BAD_REQUEST::equals,
                    response -> Mono.error(new InvalidWordException(word))
                )
                .onStatus(
                    status -> status.is4xxClientError(),
                    response -> Mono.error(new DictionaryApiException(
                        "Client error occurred while fetching dictionary entry for word: " + word,
                        response.statusCode().value(),
                        "CLIENT_ERROR"
                    ))
                )
                .onStatus(
                    status -> status.is5xxServerError(),
                    response -> Mono.error(new DictionaryApiException(
                        "Server error occurred while fetching dictionary entry for word: " + word,
                        response.statusCode().value(),
                        "SERVER_ERROR"
                    ))
                )
                .bodyToMono(DICT_REFERENCE)
                .onErrorMap(WebClientResponseException.class, ex -> {
                    if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                        return new WordNotFoundException(word);
                    } else if (ex.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                        return new RateLimitExceededException();
                    } else if (ex.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
                        return new DictionaryServiceUnavailableException();
                    } else if (ex.getStatusCode() == HttpStatus.BAD_REQUEST) {
                        return new InvalidWordException(word);
                    } else {
                        return new DictionaryApiException(
                            "Error occurred while fetching dictionary entry for word: " + word + ". " + ex.getResponseBodyAsString(),
                            ex.getStatusCode().value(),
                            "API_ERROR"
                        );
                    }
                })
                .onErrorMap(Exception.class, ex -> {
                    if (ex instanceof DictionaryApiException) {
                        return ex;
                    }
                    return new DictionaryApiException(
                        "Unexpected error occurred while fetching dictionary entry for word: " + word,
                        500,
                        "UNEXPECTED_ERROR"
                    );
                });
    }

}
