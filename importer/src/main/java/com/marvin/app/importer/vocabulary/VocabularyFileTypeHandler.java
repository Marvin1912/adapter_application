package com.marvin.app.importer.vocabulary;

import com.marvin.app.service.FileTypeHandler;
import com.marvin.influxdb.core.InfluxWriteConfig;
import com.marvin.vocabulary.dto.Flashcard;
import org.springframework.stereotype.Component;

@Component
public class VocabularyFileTypeHandler implements FileTypeHandler<Flashcard> {

    public static final String FILE_TYPE_PREFIX = "vocabulary";

    private final VocabularyImportService vocabularyImportService;

    public VocabularyFileTypeHandler(VocabularyImportService vocabularyImportService) {
        this.vocabularyImportService = vocabularyImportService;
    }

    @Override
    public boolean canHandle(String fileType) {
        return FILE_TYPE_PREFIX.equals(fileType);
    }

    @Override
    public Class<Flashcard> getDtoClass() {
        return Flashcard.class;
    }

    @Override
    public String getBucket() {
        return "vocabulary"; // Not used for InfluxDB
    }

    @Override
    public void handle(InfluxWriteConfig config, Flashcard flashcard) {
        vocabularyImportService.importData(config, flashcard);
    }
}