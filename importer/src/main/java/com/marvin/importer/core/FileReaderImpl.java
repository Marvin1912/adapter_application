package com.marvin.importer.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FileReaderImpl implements FileReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileReaderImpl.class);

    private final String in;
    private final ObjectMapper objectMapper;
    private final FileArchiveService fileArchiveService;

    public FileReaderImpl(
            @Value("${importer.in}") String in,
            ObjectMapper objectMapper,
            FileArchiveService fileArchiveService
    ) {
        this.in = in;
        this.objectMapper = objectMapper;
        this.fileArchiveService = fileArchiveService;
    }

    @Override
    public void importFiles() {
        try (Stream<Path> pathStream = Files.walk(Path.of(in), 1)) {
            pathStream
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(this::readDataFromFile);
        } catch (Exception e) {
            LOGGER.error("Could read files!", e);
        }
    }

    @Override
    public void processFile(Path path, List<FileTypeHandler<?>> handlers) {
        final FilePatternMatcher.FileTypeMatchResult matchResult =
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

        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(line -> processLine(line, handler));
        } catch (Exception e) {
            LOGGER.error("Could read lines from {}!", path.getFileName(), e);
        }

        fileArchiveService.moveToDone(path);
    }

    private void readDataFromFile(Path path) {
        processFile(path, List.of());
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
            ((FileTypeHandler<Object>) handler).send(dto);
        } catch (Exception e) {
            LOGGER.error("Could not process line: {}", line, e);
        }
    }
}