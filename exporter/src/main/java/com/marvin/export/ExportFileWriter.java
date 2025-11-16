package com.marvin.export;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ExportFileWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportFileWriter.class);

    private final ObjectMapper objectMapper;

    public ExportFileWriter(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "ObjectMapper must not be null");
    }

    public <T> void writeFile(Path target, Stream<T> dataStream) {
        Objects.requireNonNull(target, "Target path must not be null");
        Objects.requireNonNull(dataStream, "Data stream must not be null");

        LOGGER.info("Starting to write file: {}", target);

        try (BufferedWriter writer = Files.newBufferedWriter(target)) {
            writeStreamToJsonLines(writer, dataStream);
            LOGGER.info("Successfully wrote file: {}", target);
        } catch (IOException e) {
            throw new FileWriteException("Failed to write file: " + target, e);
        }
    }

    private <T> void writeStreamToJsonLines(BufferedWriter writer, Stream<T> dataStream) throws IOException {
        try {
            dataStream.forEach(item -> writeJsonLine(writer, item));
        } catch (RuntimeException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            }
            throw e;
        }
    }

    private <T> void writeJsonLine(BufferedWriter writer, T item) {
        try {
            final String json = objectMapper.writeValueAsString(item);
            writer.write(json);
            writer.newLine();
        } catch (JsonProcessingException e) {
            throw new JsonSerializationException("Failed to serialize object: " + item, e);
        } catch (IOException e) {
            throw new FileWriteException("Failed to write JSON line to file", e);
        }
    }

    public static class FileWriteException extends RuntimeException {

        public FileWriteException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class JsonSerializationException extends RuntimeException {

        public JsonSerializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
