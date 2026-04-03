package com.marvin.vocabulary.controller;

import com.marvin.backup.repository.BackupRunRepository;
import com.marvin.backup.entity.BackupRunEntity;
import com.marvin.vocabulary.dto.AnkiSyncRunRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/vocabulary")
@RequiredArgsConstructor
public class SyncRunController {

    private static final String ANKI_SYNC_FILE_NAME = "anki-sync";

    private final BackupRunRepository backupRunRepository;

    @PostMapping("/sync-runs")
    public Mono<ResponseEntity<Void>> recordSyncRun(@RequestBody AnkiSyncRunRequest request) {
        return Mono.fromRunnable(() -> {
            BackupRunEntity entity = new BackupRunEntity();
            entity.setFileName(ANKI_SYNC_FILE_NAME);
            entity.setStatus(request.status());

            long durationMs = request.durationMs() != null ? request.durationMs() : 0L;
            LocalDateTime finishedAt = LocalDateTime.now();
            entity.setFinishedAt(finishedAt);
            entity.setStartedAt(finishedAt.minus(Duration.ofMillis(durationMs)));
            entity.setDurationMs(durationMs);
            entity.setUploadSuccess("SUCCESS".equals(request.status()));
            entity.setErrorMessage(request.errorMessage());

            backupRunRepository.save(entity);
        })
        .subscribeOn(Schedulers.boundedElastic())
        .thenReturn(ResponseEntity.noContent().<Void>build());
    }
}
