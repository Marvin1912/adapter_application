package com.marvin.upload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FileDeleter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileDeleter.class);

    private final GoogleDrive googleDrive;

    public FileDeleter(GoogleDrive googleDrive) {
        this.googleDrive = googleDrive;
    }

    public void deleteFile(String fileId) {
        LOGGER.info("Deleting file with ID: {}", fileId);

        try {
            googleDrive.deleteFile(fileId);
            LOGGER.info("Successfully deleted file with ID: {}", fileId);
        } catch (GoogleDriveException e) {
            LOGGER.error("Failed to delete file with ID: {}", fileId, e);
            throw new RuntimeException("Failed to delete file with ID: " + fileId, e);
        }
    }
}