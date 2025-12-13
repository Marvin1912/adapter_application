package com.marvin.export.vocabulary;

import com.marvin.export.core.AbstractExporterBase;
import com.marvin.export.core.ExportConfig;
import com.marvin.export.core.ExportFileWriter;
import com.marvin.vocabulary.dto.Flashcard;
import com.marvin.vocabulary.service.FlashcardService;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class VocabularyExporter extends AbstractExporterBase {

    public static final String FILENAME_PREFIX = "vocabulary";
    private static final String FILE_EXTENSION = ".jsonl";

    private final FlashcardService flashcardService;

    public VocabularyExporter(ExportConfig exportConfig, ExportFileWriter exportFileWriter, FlashcardService flashcardService) {
        super(exportConfig, exportFileWriter);
        this.flashcardService = flashcardService;
    }

    public List<Path> exportVocabulary() {
        final String timestamp = getCurrentTimestamp();
        final String exportFolder = exportConfig.getCostExportFolder();

        final Path vocabularyPath = createFilePath(exportFolder, FILENAME_PREFIX, timestamp, FILE_EXTENSION);
        exportData(vocabularyPath, this::createVocabularyStream);

        return List.of(vocabularyPath.getFileName());
    }

    @Override
    protected List<Path> export() {
        return exportVocabulary();
    }

    private Stream<Flashcard> createVocabularyStream() {
        return flashcardService.getAllFlashcardsForExport();
    }
}
