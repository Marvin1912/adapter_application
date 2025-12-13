package com.marvin.app.importer.vocabulary;

import com.marvin.app.service.ImportService;
import com.marvin.influxdb.core.InfluxWriteConfig;
import com.marvin.vocabulary.dto.Flashcard;
import com.marvin.vocabulary.model.FlashcardEntity;
import com.marvin.vocabulary.service.FlashcardService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class VocabularyImportService implements ImportService<Flashcard> {

    private final FlashcardService flashcardService;

    public VocabularyImportService(FlashcardService flashcardService) {
        this.flashcardService = flashcardService;
    }

    @Override
    @Transactional
    public void importData(InfluxWriteConfig config, Flashcard flashcard) {
        if (flashcard == null) {
            return;
        }

        final FlashcardEntity entity = new FlashcardEntity(
            null,
            flashcard.deck(),
            flashcard.ankiId(),
            flashcard.front(),
            flashcard.back(),
            flashcard.description(),
            flashcard.updated()
        );

        flashcardService.save(entity);
    }
}