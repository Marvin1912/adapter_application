package com.marvin.vocabulary.controller;

import com.marvin.vocabulary.dto.Deck;
import com.marvin.vocabulary.model.DeckEntity;
import com.marvin.vocabulary.service.DeckService;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/vocabulary")
public class DeckController {

    private final DeckService deckService;

    public DeckController(DeckService deckService) {
        this.deckService = deckService;
    }

    private static Deck toDto(DeckEntity entity) {
        return new Deck(entity.getId(), entity.getName());
    }

    @GetMapping("/decks/{id}")
    public Mono<ResponseEntity<Deck>> getDeck(@PathVariable int id) {
        DeckEntity entity = deckService.get(id);
        if (entity == null) {
            return Mono.just(ResponseEntity.notFound().build());
        }
        return Mono.just(ResponseEntity.ok(toDto(entity)));
    }

    @GetMapping("/decks")
    public Flux<Deck> getDecks() {
        List<DeckEntity> decks = deckService.getAll();
        return Flux.fromIterable(decks).map(DeckController::toDto);
    }

    @PostMapping("/decks")
    public Mono<ResponseEntity<Void>> createDeck(@RequestBody Mono<Deck> deckMono) {
        return deckMono
            .map(deckService::create)
            .map(saved -> ResponseEntity.created(
                URI.create("/decks/" + saved.getId())
            ).build());
    }

    @PutMapping("/decks")
    public Mono<ResponseEntity<Deck>> updateDeck(@RequestBody Mono<Deck> deckMono) {
        return deckMono
            .map(deckService::update)
            .map(deckEntity -> ResponseEntity.ok(toDto(deckEntity)));
    }
}
