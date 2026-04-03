package com.marvin.backup.service;

import com.marvin.backup.repository.BackupRunRepository;
import com.marvin.entities.exports.BackupRunEntity;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Tracks the lifecycle of backup upload runs by persisting their status to the database. */
@Service
@RequiredArgsConstructor
public class BackupTrackingService {

    private final BackupRunRepository backupRunRepository;

    /**
     * Starts a new backup run for the given file name.
     *
     * @param fileName the name of the backup file being uploaded
     * @return the persisted backup run entity in IN_PROGRESS state
     */
    public BackupRunEntity start(String fileName) {
        final BackupRunEntity run = new BackupRunEntity();
        run.setFileName(fileName);
        run.setStatus("IN_PROGRESS");
        run.setStartedAt(LocalDateTime.now());
        return backupRunRepository.save(run);
    }

    /**
     * Marks the given backup run as successfully completed.
     *
     * @param run the backup run entity to update
     */
    public void completeSuccess(BackupRunEntity run) {
        run.setStatus("SUCCESS");
        run.setFinishedAt(LocalDateTime.now());
        run.setDurationMs(Duration.between(run.getStartedAt(), run.getFinishedAt()).toMillis());
        run.setUploadSuccess(true);
        backupRunRepository.save(run);
    }

    /**
     * Marks the given backup run as failed with the provided error message.
     *
     * @param run          the backup run entity to update
     * @param errorMessage the error message describing the failure
     */
    public void completeFailure(BackupRunEntity run, String errorMessage) {
        run.setStatus("FAILED");
        run.setFinishedAt(LocalDateTime.now());
        run.setDurationMs(Duration.between(run.getStartedAt(), run.getFinishedAt()).toMillis());
        run.setUploadSuccess(false);
        run.setErrorMessage(errorMessage);
        backupRunRepository.save(run);
    }

}
