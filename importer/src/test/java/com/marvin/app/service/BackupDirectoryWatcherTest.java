package com.marvin.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import com.marvin.app.model.event.BackupFileEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class BackupDirectoryWatcherTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @TempDir
    Path tempDir;

    private BackupDirectoryWatcher watcher;

    @BeforeEach
    void setUp() {
        watcher = new BackupDirectoryWatcher(eventPublisher, tempDir.toString());
    }

    @AfterEach
    void tearDown() {
        watcher.stopWatchService();
    }

    @Test
    void shouldPublishBackupFileEventWhenZipFileCreated() throws Exception {
        // Start watcher in a separate thread
        Thread watcherThread = new Thread(() -> watcher.startWatchService());
        watcherThread.setDaemon(true);
        watcherThread.start();

        // Give the watcher time to register
        Thread.sleep(500);

        // Create a backup zip file in the watched directory
        Path backupFile = tempDir.resolve("costs_backup_20260301_120000.zip");
        Files.writeString(backupFile, "fake-backup-content");

        // Verify the event was published (with timeout for async processing)
        ArgumentCaptor<BackupFileEvent> captor = ArgumentCaptor.forClass(BackupFileEvent.class);
        verify(eventPublisher, timeout(5000)).publishEvent(captor.capture());

        BackupFileEvent event = captor.getValue();
        assertThat(event.path().getFileName().toString())
                .isEqualTo("costs_backup_20260301_120000.zip");
    }

    @Test
    void shouldIgnoreNonZipFiles() throws Exception {
        // Start watcher in a separate thread
        Thread watcherThread = new Thread(() -> watcher.startWatchService());
        watcherThread.setDaemon(true);
        watcherThread.start();

        // Give the watcher time to register
        Thread.sleep(500);

        // Create a non-zip file
        Path textFile = tempDir.resolve("notes.txt");
        Files.writeString(textFile, "not a backup");

        // Wait a bit and verify no event was published
        Thread.sleep(1000);
        verify(eventPublisher, timeout(1000).times(0)).publishEvent(any(BackupFileEvent.class));
    }

    @Test
    void shouldCreateWatchDirectoryIfNotExists() throws Exception {
        Path nonExistentDir = tempDir.resolve("new-watch-dir");
        BackupDirectoryWatcher newWatcher = new BackupDirectoryWatcher(
                eventPublisher, nonExistentDir.toString());

        // Start and immediately stop — just testing directory creation
        Thread watcherThread = new Thread(() -> newWatcher.startWatchService());
        watcherThread.setDaemon(true);
        watcherThread.start();

        Thread.sleep(500);
        newWatcher.stopWatchService();

        assertThat(nonExistentDir).exists();
    }
}
