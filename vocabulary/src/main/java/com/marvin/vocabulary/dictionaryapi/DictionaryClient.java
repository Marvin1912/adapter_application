package com.marvin.vocabulary.dictionaryapi;

import com.marvin.vocabulary.dto.DictionaryEntry;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
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
        return webClient.get()
                .uri("/" + word)
                .retrieve()
                .bodyToMono(DICT_REFERENCE);
    }

}
