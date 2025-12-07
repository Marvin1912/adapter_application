package com.marvin.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.influxdb.client.domain.WritePrecision;
import com.marvin.app.service.FilePatternMatcher.FileTypeMatchResult;
import com.marvin.influxdb.core.InfluxWriteConfig;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GenericFileReaderImpl implements GenericFileReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericFileReaderImpl.class);
    private static final int PROGRESS_INTERVAL = 500;

    private final String org;
    private final ObjectMapper objectMapper;
    private final FileArchiveService fileArchiveService;

    public GenericFileReaderImpl(
        @Value("${influxdb.org}") String org,
        ObjectMapper objectMapper,
        FileArchiveService fileArchiveService
    ) {
        this.org = org;
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

        final InfluxWriteConfig config = InfluxWriteConfig.create(handler.getBucket(), org, WritePrecision.NS);

        final AtomicLong totalItems = new AtomicLong(0);
        final AtomicLong processedItems = new AtomicLong(0);

        try {
            // Count total lines first
            long fileLines = Files.lines(path).count();
            totalItems.addAndGet(fileLines);
            LOGGER.info("File {} has {} lines to process", path.getFileName(), fileLines);

            // Process lines
            try (Stream<String> lines = Files.lines(path)) {
                lines.forEach(line -> {
                    processLine(config, line, handler);
                    processedItems.incrementAndGet();
                    logProgress(processedItems, totalItems);
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
    private void processLine(InfluxWriteConfig config, String line, FileTypeHandler<?> handler) {
        try {
            Object dto = handler.readValue(line, objectMapper);
            ((FileTypeHandler<Object>) handler).handle(config, dto);
        } catch (Exception e) {
            LOGGER.error("Could not process line: {}", line, e);
        }
    }

    private void logProgress(AtomicLong processedItems, AtomicLong totalItems) {
        if (processedItems.get() % PROGRESS_INTERVAL == 0) {
            double percentage = totalItems.get() > 0 ? (processedItems.get() * 100.0 / totalItems.get()) : 0;
            LOGGER.info("Progress: {}/{} items processed ({}%)", processedItems, totalItems,
                BigDecimal.valueOf(percentage).setScale(2, RoundingMode.HALF_UP));
        }
    }
}
