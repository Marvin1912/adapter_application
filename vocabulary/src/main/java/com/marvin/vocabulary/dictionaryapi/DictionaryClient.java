package com.marvin.vocabulary.dictionaryapi;

import com.marvin.vocabulary.dto.DictionaryEntry;
import com.marvin.vocabulary.exceptions.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
public class DictionaryClient {

    private static final ParameterizedTypeReference<Map<String, List<WiktionaryResponseMapper.WiktionaryDefinition>>> WIKTIONARY_REFERENCE =
            new ParameterizedTypeReference<>() {
            };

    private final WebClient webClient;
    private final WiktionaryResponseMapper responseMapper;

    public DictionaryClient(WiktionaryResponseMapper responseMapper) {
        this.responseMapper = responseMapper;
        this.webClient = WebClient.builder()
                .baseUrl("https://en.wiktionary.org/api/rest_v1")
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
                .uri("/page/definition/" + word.trim())
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
                .bodyToMono(WIKTIONARY_REFERENCE)
                .map(response -> responseMapper.mapToDictionaryEntries(word, response))
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
