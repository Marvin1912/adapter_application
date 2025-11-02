package com.marvin.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ExportFileWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportFileWriter.class.getName());

    private final ObjectMapper objectMapper;

    public ExportFileWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> void writeFile(Path target, Stream<T> dataStream) {

        LOGGER.info("Going to write file {}!", target);

        try (BufferedWriter writer = Files.newBufferedWriter(target)) {
            dataStream.forEach(item -> {
                try {
                    String json = objectMapper.writeValueAsString(item);
                    writer.write(json);
                    writer.newLine();
                } catch (IOException e) {
                    throw new RuntimeException("Could not write object " + item + "!", e);
                }
            });
            LOGGER.info("File {} has been written!", target);
        } catch (IOException e) {
            throw new RuntimeException("Could not open file " + target + "!", e);
        }
    }
}
