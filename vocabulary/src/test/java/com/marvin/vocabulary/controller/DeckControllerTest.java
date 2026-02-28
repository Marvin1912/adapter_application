package com.marvin.vocabulary.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.marvin.vocabulary.dto.Deck;
import com.marvin.vocabulary.model.DeckEntity;
import com.marvin.vocabulary.service.DeckService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@ExtendWith(MockitoExtension.class)
class DeckControllerTest {

    private final DeckEntity testDeckEntity = new DeckEntity(10, "test-deck", null);
    private final Deck testDeck = new Deck(10, "test-deck");

    @Mock
    private DeckService deckService;

    @InjectMocks
    private DeckController deckController;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(deckController).build();
    }

    @Test
    void getDeckWhenExistsShouldReturnDeck() {
        when(deckService.get(10)).thenReturn(testDeckEntity);

        webTestClient.get()
                .uri("/vocabulary/decks/10")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Deck.class)
                .isEqualTo(testDeck);
    }

    @Test
    void getDeckWhenMissingShouldReturnNotFound() {
        when(deckService.get(999)).thenReturn(null);

        webTestClient.get()
                .uri("/vocabulary/decks/999")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getDecksShouldReturnAllDecks() {
        when(deckService.getAll()).thenReturn(List.of(testDeckEntity));

        webTestClient.get()
                .uri("/vocabulary/decks")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Deck.class)
                .isEqualTo(List.of(testDeck));
    }

    @Test
    void createDeckShouldReturnLocation() {
        Deck input = new Deck(null, "new-deck");
        DeckEntity saved = new DeckEntity(11, "new-deck", null);
        when(deckService.create(any(Deck.class))).thenReturn(saved);

        webTestClient.post()
                .uri("/vocabulary/decks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(input)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().valueEquals("Location", "/decks/11")
                .expectBody().isEmpty();
    }
}
