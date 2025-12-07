package com.marvin.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marvin.app.service.FilePatternMatcher.FileTypeMatchResult;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GenericFileReaderImpl implements GenericFileReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericFileReaderImpl.class);
    private static final int PROGRESS_INTERVAL = 500;

    private final ObjectMapper objectMapper;
    private final FileArchiveService fileArchiveService;
    private long totalItems = 0;
    private long processedItems = 0;

    public GenericFileReaderImpl(
        ObjectMapper objectMapper,
        FileArchiveService fileArchiveService
    ) {
        this.objectMapper = objectMapper;
        this.fileArchiveService = fileArchiveService;
    }

    @Override
    public void importFiles(String in, List<FileTypeHandler<?>> handlers) {
        try (Stream<Path> pathStream = Files.walk(Path.of(in), 1)) {
            pathStream
                .filter(path -> !Files.isDirectory(path))
                .forEach(path -> processFile(path, handlers));
        } catch (Exception e) {
            LOGGER.error("Could read files!", e);
        }
    }

    private void processFile(Path path, List<FileTypeHandler<?>> handlers) {

        LOGGER.info("Processing file {}", path);

        final FileTypeMatchResult matchResult =
            FilePatternMatcher.matchFileName(path.getFileName().toString());

        if (!matchResult.matches()) {
            LOGGER.warn("Could not match file name pattern for {}", path);
            return;
        }

        final String fileType = matchResult.fileType();
        final FileTypeHandler<?> handler = findHandler(fileType, handlers);

        if (handler == null) {
            LOGGER.warn("Could not find matching handler for {}!", fileType);
            return;
        }

        try {
            // Count total lines first
            long fileLines = Files.lines(path).count();
            totalItems += fileLines;
            LOGGER.info("File {} has {} lines to process", path.getFileName(), fileLines);

            // Reset processed counter for this file
            processedItems = 0;

            // Process lines
            try (Stream<String> lines = Files.lines(path)) {
                lines.forEach(line -> {
                    processLine(line, handler);
                    processedItems++;
                    logProgress();
                });
            }
        } catch (Exception e) {
            LOGGER.error("Could read lines from {}!", path.getFileName(), e);
        }

        fileArchiveService.moveToDone(path);

        LOGGER.info("File {} has been moved to done!", path);
    }

    private FileTypeHandler<?> findHandler(String fileType, List<FileTypeHandler<?>> handlers) {
        return handlers.stream()
            .filter(handler -> handler.canHandle(fileType))
            .findFirst()
            .orElse(null);
    }

    @SuppressWarnings("unchecked")
    private void processLine(String line, FileTypeHandler<?> handler) {
        try {
            Object dto = handler.readValue(line, objectMapper);
            ((FileTypeHandler<Object>) handler).handle(dto);
        } catch (Exception e) {
            LOGGER.error("Could not process line: {}", line, e);
        }
    }

    private void logProgress() {
        if (processedItems % PROGRESS_INTERVAL == 0) {
            double percentage = totalItems > 0 ? (processedItems * 100.0 / totalItems) : 0;
            LOGGER.info("Progress: {}/{} items processed ({}%)", processedItems, totalItems,
                BigDecimal.valueOf(percentage).setScale(2, RoundingMode.HALF_UP));
        }
    }
}
