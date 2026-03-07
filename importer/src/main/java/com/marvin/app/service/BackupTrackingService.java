package com.marvin.app.service;

import com.marvin.database.repository.BackupRunRepository;
import com.marvin.entities.exports.BackupRunEntity;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BackupTrackingService {

    private final BackupRunRepository backupRunRepository;

    public BackupRunEntity start(String fileName) {
        BackupRunEntity run = new BackupRunEntity();
        run.setFileName(fileName);
        run.setStatus("IN_PROGRESS");
        run.setStartedAt(LocalDateTime.now());
        return backupRunRepository.save(run);
    }

    public void completeSuccess(BackupRunEntity run) {
        run.setStatus("SUCCESS");
        run.setFinishedAt(LocalDateTime.now());
        run.setDurationMs(Duration.between(run.getStartedAt(), run.getFinishedAt()).toMillis());
        run.setUploadSuccess(true);
        backupRunRepository.save(run);
    }

    public void completeFailure(BackupRunEntity run, String errorMessage) {
        run.setStatus("FAILED");
        run.setFinishedAt(LocalDateTime.now());
        run.setDurationMs(Duration.between(run.getStartedAt(), run.getFinishedAt()).toMillis());
        run.setUploadSuccess(false);
        run.setErrorMessage(errorMessage);
        backupRunRepository.save(run);
    }

}
