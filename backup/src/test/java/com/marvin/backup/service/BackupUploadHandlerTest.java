package com.marvin.backup.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.marvin.backup.model.event.BackupFileEvent;
import com.marvin.upload.Uploader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BackupUploadHandlerTest {

    private static final String DRIVE_FOLDER = "db-backups";

    @Mock
    private Uploader uploader;

    @Mock
    private BackupTrackingService backupTrackingService;

    @TempDir
    private Path tempDir;

    private Path doneDir;
    private Path errorDir;
    private BackupUploadHandler handler;

    @BeforeEach
    void setUp() {
        doneDir = tempDir.resolve("done");
        errorDir = tempDir.resolve("error");
        handler = new BackupUploadHandler(
                uploader,
                backupTrackingService,
                doneDir.toString(),
                errorDir.toString(),
                DRIVE_FOLDER
        );
    }

    @Test
    void shouldUploadAndMoveFileToDone() throws IOException {
        final Path backupFile = tempDir.resolve("costs_backup_20260301_120000.zip");
        Files.writeString(backupFile, "fake-backup-content");

        handler.handleBackupFile(new BackupFileEvent(backupFile));

        verify(uploader).uploadFile(backupFile, DRIVE_FOLDER);
        assertThat(doneDir.resolve(backupFile.getFileName())).exists();
        assertThat(backupFile).doesNotExist();
    }

    @Test
    void shouldMoveFileToErrorOnUploadFailure() throws IOException {
        final Path backupFile = tempDir.resolve("costs_backup_20260301_120000.zip");
        Files.writeString(backupFile, "fake-backup-content");

        doThrow(new IllegalStateException("Upload failed"))
                .when(uploader).uploadFile(backupFile, DRIVE_FOLDER);

        handler.handleBackupFile(new BackupFileEvent(backupFile));

        assertThat(errorDir.resolve(backupFile.getFileName())).exists();
        assertThat(backupFile).doesNotExist();
    }

    @Test
    void shouldCreateDoneAndErrorDirectories() {
        assertThat(doneDir).exists();
        assertThat(errorDir).exists();
    }
}
