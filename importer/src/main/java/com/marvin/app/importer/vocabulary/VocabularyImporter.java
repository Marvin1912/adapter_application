package com.marvin.app.importer.vocabulary;

import com.marvin.app.service.FileTypeHandler;
import com.marvin.app.service.GenericFileReader;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class VocabularyImporter {

    private final String in;
    private final GenericFileReader genericFileReader;
    private final List<FileTypeHandler<?>> fileTypeHandlers;

    public VocabularyImporter(
        @Value("${importer.in.vocabulary}") String in,
        GenericFileReader genericFileReader,
        VocabularyFileTypeHandler vocabularyFileTypeHandler
    ) {
        this.in = in;
        this.genericFileReader = genericFileReader;
        this.fileTypeHandlers = List.of(vocabularyFileTypeHandler);
    }

    public void importFiles() {
        genericFileReader.importFiles(in, fileTypeHandlers);
    }
}