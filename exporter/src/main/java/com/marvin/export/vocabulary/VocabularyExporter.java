package com.marvin.export.vocabulary;

import com.marvin.export.AbstractExporterBase;
import com.marvin.export.ExportConfig;
import com.marvin.export.ExportFileWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class VocabularyExporter extends AbstractExporterBase {

    public static final String FILENAME_PREFIX = "vocabulary";
    private static final String FILE_EXTENSION = ".json";

    public VocabularyExporter(ExportConfig exportConfig, ExportFileWriter exportFileWriter) {
        super(exportConfig, exportFileWriter);
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

    private Stream<String> createVocabularyStream() {
        return Stream.of("vocabulary data placeholder");
    }
}
