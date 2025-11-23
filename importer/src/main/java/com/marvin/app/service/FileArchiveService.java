package com.marvin.app.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FileArchiveService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileArchiveService.class);

    private final String doneDirectory;

    public FileArchiveService(@Value("${importer.done}") String doneDirectory) {
        this.doneDirectory = doneDirectory;
    }

    public void moveToDone(Path file) {
        try {
            Files.move(file, Path.of(doneDirectory).resolve(file.getFileName()),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOGGER.error("Could not move file {} to done!", file, e);
        }
    }
}