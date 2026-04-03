package com.marvin.backup.service;

import com.marvin.backup.model.event.BackupFileEvent;
import com.marvin.backup.entity.BackupRunEntity;
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

/** Handles {@link BackupFileEvent} events by uploading backup files to Google Drive. */
@Component
@ConditionalOnProperty(name = "backup.upload.enabled", havingValue = "true")
public class BackupUploadHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(BackupUploadHandler.class);

    private final Uploader uploader;
    private final BackupTrackingService backupTrackingService;
    private final Path doneDirectory;
    private final Path errorDirectory;
    private final String driveFolderName;

    /**
     * Constructs a new {@code BackupUploadHandler}.
     *
     * @param uploader              the uploader for transferring files to Google Drive
     * @param backupTrackingService the service for tracking backup run status
     * @param doneDir               the directory to move successfully uploaded files to
     * @param errorDir              the directory to move failed uploads to
     * @param driveFolderName       the Google Drive folder name to upload files into
     */
    public BackupUploadHandler(
            Uploader uploader,
            BackupTrackingService backupTrackingService,
            @Value("${backup.upload.done-dir}") String doneDir,
            @Value("${backup.upload.error-dir}") String errorDir,
            @Value("${backup.upload.drive-folder-name}") String driveFolderName
    ) {
        this.uploader = uploader;
        this.backupTrackingService = backupTrackingService;
        this.doneDirectory = Path.of(doneDir);
        this.errorDirectory = Path.of(errorDir);
        this.driveFolderName = driveFolderName;
        ensureDirectories();
    }

    /**
     * Handles a backup file event by uploading the file and tracking the outcome.
     *
     * @param event the event containing the path of the backup file to upload
     */
    @EventListener(BackupFileEvent.class)
    public void handleBackupFile(BackupFileEvent event) {
        final Path backupFile = event.path();
        LOGGER.info("Handling backup file for upload: {}", backupFile.getFileName());

        final BackupRunEntity run = backupTrackingService.start(
                backupFile.getFileName().toString());

        try {
            uploader.uploadFile(backupFile, driveFolderName);
            moveFile(backupFile, doneDirectory);
            backupTrackingService.completeSuccess(run);
            LOGGER.info("Backup file {} uploaded and moved to done.", backupFile.getFileName());
        } catch (Exception e) {
            LOGGER.error("Failed to upload backup file: {}", backupFile.getFileName(), e);
            backupTrackingService.completeFailure(run, e.getMessage());
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
