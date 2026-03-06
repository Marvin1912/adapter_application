package com.marvin.app.service;

import com.marvin.app.model.event.BackupFileEvent;
import com.marvin.upload.Uploader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "backup.upload.enabled", havingValue = "true")
public class BackupUploadHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(BackupUploadHandler.class);

    private final Uploader uploader;
    private final Path doneDirectory;
    private final Path errorDirectory;
    private final String driveFolderName;

    public BackupUploadHandler(
            Uploader uploader,
            @Value("${backup.upload.done-dir}") String doneDir,
            @Value("${backup.upload.error-dir}") String errorDir,
            @Value("${backup.upload.drive-folder-name}") String driveFolderName
    ) {
        this.uploader = uploader;
        this.doneDirectory = Path.of(doneDir);
        this.errorDirectory = Path.of(errorDir);
        this.driveFolderName = driveFolderName;
        ensureDirectories();
    }

    @EventListener(BackupFileEvent.class)
    public void handleBackupFile(BackupFileEvent event) {
        final Path backupFile = event.path();
        LOGGER.info("Handling backup file for upload: {}", backupFile.getFileName());

        try {
            uploader.uploadFile(backupFile, driveFolderName);
            moveFile(backupFile, doneDirectory);
            LOGGER.info("Backup file {} uploaded and moved to done.", backupFile.getFileName());
        } catch (Exception e) {
            LOGGER.error("Failed to upload backup file: {}", backupFile.getFileName(), e);
            moveFile(backupFile, errorDirectory);
        }
    }

    private void moveFile(Path file, Path targetDirectory) {
        try {
            final Path target = targetDirectory.resolve(file.getFileName());
            Files.move(file, target, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info("Moved {} to {}", file.getFileName(), targetDirectory);
        } catch (IOException e) {
            LOGGER.error("Could not move file {} to {}!", file.getFileName(), targetDirectory, e);
        }
    }

    private void ensureDirectories() {
        try {
            if (!Files.exists(doneDirectory)) {
                Files.createDirectories(doneDirectory);
            }
            if (!Files.exists(errorDirectory)) {
                Files.createDirectories(errorDirectory);
            }
        } catch (IOException e) {
            LOGGER.error("Could not create backup upload directories!", e);
        }
    }
}
