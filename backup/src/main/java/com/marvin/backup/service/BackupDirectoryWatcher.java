package com.marvin.backup.service;

import com.marvin.backup.model.event.BackupFileEvent;
import jakarta.annotation.PreDestroy;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Watches a directory for new backup files and publishes {@link BackupFileEvent} events. */
@Component
@ConditionalOnProperty(name = "backup.upload.enabled", havingValue = "true")
public class BackupDirectoryWatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(BackupDirectoryWatcher.class);

    private static final AtomicBoolean IS_RUNNING = new AtomicBoolean();

    private final ApplicationEventPublisher eventPublisher;
    private final Path watchDirectory;

    /**
     * Constructs a new {@code BackupDirectoryWatcher}.
     *
     * @param eventPublisher the Spring application event publisher
     * @param watchDir       the path of the directory to watch for new backup files
     */
    public BackupDirectoryWatcher(
            ApplicationEventPublisher eventPublisher,
            @Value("${backup.upload.watch-dir}") String watchDir
    ) {
        this.eventPublisher = eventPublisher;
        this.watchDirectory = Path.of(watchDir);
    }

    /** Starts the watch service when the application is ready. */
    @EventListener(ApplicationReadyEvent.class)
    public void startWatchService() {
        new Thread(() -> {
            IS_RUNNING.set(true);
            try {
                ensureDirectoryExists();
                watchDirectory();
            } catch (Exception e) {
                LOGGER.error("Error starting backup WatchService!", e);
            }
        }).start();
    }

    /** Stops the watch service on application shutdown. */
    @PreDestroy
    public void stopWatchService() {
        IS_RUNNING.set(false);
    }

    private void ensureDirectoryExists() throws Exception {
        if (!Files.exists(watchDirectory)) {
            Files.createDirectories(watchDirectory);
            LOGGER.info("Created backup watch directory: {}", watchDirectory);
        }
    }

    private void watchDirectory() throws Exception {
        LOGGER.info("Watching backup directory: {}", watchDirectory);

        final WatchService watchService = FileSystems.getDefault().newWatchService();
        watchDirectory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

        WatchKey key;
        while (IS_RUNNING.get() && (key = watchService.take()) != null) {
            for (WatchEvent<?> event : key.pollEvents()) {
                final Path file = Paths.get(watchDirectory.toString(),
                        event.context().toString());
                if (!Files.isDirectory(file) && isBackupFile(file)) {
                    LOGGER.info("Detected new backup file: {}", file.getFileName());
                    eventPublisher.publishEvent(new BackupFileEvent(file));
                }
            }
            key.reset();
        }
    }

    private boolean isBackupFile(Path file) {
        return file.getFileName().toString().endsWith(".zip");
    }
}
