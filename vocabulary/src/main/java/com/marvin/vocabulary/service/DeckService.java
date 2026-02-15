package com.marvin.vocabulary.service;

import com.marvin.vocabulary.dto.Deck;
import com.marvin.vocabulary.model.DeckEntity;
import com.marvin.vocabulary.repository.DeckRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DeckService {

    private final DeckRepository deckRepository;

    public DeckService(DeckRepository deckRepository) {
        this.deckRepository = deckRepository;
    }

    public DeckEntity get(int id) {
        return deckRepository.findById(id).orElse(null);
    }

    public List<DeckEntity> getAll() {
        return deckRepository.findAll();
    }

    public DeckEntity create(Deck deck) {
        DeckEntity entity = new DeckEntity();
        entity.setName(deck.name());
        return deckRepository.save(entity);
    }
}
