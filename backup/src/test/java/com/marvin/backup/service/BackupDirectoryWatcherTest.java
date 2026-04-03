package com.marvin.backup.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import com.marvin.backup.model.event.BackupFileEvent;
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
    private Path tempDir;

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
        final Thread watcherThread = new Thread(() -> watcher.startWatchService());
        watcherThread.setDaemon(true);
        watcherThread.start();

        Thread.sleep(500);

        final Path backupFile = tempDir.resolve("costs_backup_20260301_120000.zip");
        Files.writeString(backupFile, "fake-backup-content");

        final ArgumentCaptor<BackupFileEvent> captor =
                ArgumentCaptor.forClass(BackupFileEvent.class);
        verify(eventPublisher, timeout(5000)).publishEvent(captor.capture());

        final BackupFileEvent event = captor.getValue();
        assertThat(event.path().getFileName().toString())
                .isEqualTo("costs_backup_20260301_120000.zip");
    }

    @Test
    void shouldIgnoreNonZipFiles() throws Exception {
        final Thread watcherThread = new Thread(() -> watcher.startWatchService());
        watcherThread.setDaemon(true);
        watcherThread.start();

        Thread.sleep(500);

        final Path textFile = tempDir.resolve("notes.txt");
        Files.writeString(textFile, "not a backup");

        Thread.sleep(1000);
        verify(eventPublisher, timeout(1000).times(0)).publishEvent(any(BackupFileEvent.class));
    }

    @Test
    void shouldCreateWatchDirectoryIfNotExists() throws Exception {
        final Path nonExistentDir = tempDir.resolve("new-watch-dir");
        final BackupDirectoryWatcher newWatcher = new BackupDirectoryWatcher(
                eventPublisher, nonExistentDir.toString());

        final Thread watcherThread = new Thread(() -> newWatcher.startWatchService());
        watcherThread.setDaemon(true);
        watcherThread.start();

        Thread.sleep(500);
        newWatcher.stopWatchService();

        assertThat(nonExistentDir).exists();
    }
}
