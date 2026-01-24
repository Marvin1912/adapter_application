package com.marvin.api.service;

import com.marvin.api.service.ExportTrackingService.ExporterType;
import com.marvin.api.service.ExportTrackingService.Status;
import com.marvin.database.repository.ExportRunRepository;
import com.marvin.entities.exports.ExportRunEntity;
import com.marvin.upload.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportTrackingServiceTest {

    @Mock
    private ExportRunRepository exportRunRepository;

    @Mock
    private Uploader uploader;

    @InjectMocks
    private ExportTrackingService exportTrackingService;

    private ExportRunEntity savedEntity;

    @BeforeEach
    void setUp() {
        savedEntity = new ExportRunEntity();
        savedEntity.setId(1L);
        when(exportRunRepository.save(any(ExportRunEntity.class))).thenReturn(savedEntity);
    }

    @Test
    void testTrackExport_Success_WithFiles_UploadSuccess() {
        // Arrange
        ExporterType exporterType = ExporterType.COSTS;
        String exportName = "testExport";
        String requestParams = "param1=value1";
        List<Path> files = List.of(Paths.get("file1.txt"), Paths.get("file2.txt"));
        Supplier<List<Path>> exporterCall = () -> files;

        // Act
        List<Path> result = exportTrackingService.trackExport(exporterType, exportName, requestParams, exporterCall);

        // Assert
        assertEquals(files, result);
        ArgumentCaptor<ExportRunEntity> captor = ArgumentCaptor.forClass(ExportRunEntity.class);
        verify(exportRunRepository, times(2)).save(captor.capture());
        List<ExportRunEntity> savedEntities = captor.getAllValues();

        // Initial save
        ExportRunEntity initial = savedEntities.get(0);
        assertEquals(exporterType.name(), initial.getExporterType());
        assertEquals(exportName, initial.getExportName());
        assertEquals(Status.FAILED.name(), initial.getStatus());
        assertNotNull(initial.getStartedAt());
        assertEquals(requestParams, initial.getRequestParams());

        // Final save
        ExportRunEntity finalEntity = savedEntities.get(1);
        assertEquals(Status.SUCCESS.name(), finalEntity.getStatus());
        assertNotNull(finalEntity.getFinishedAt());
        assertNotNull(finalEntity.getDurationMs());
        assertEquals("file1.txt,file2.txt", finalEntity.getExportedFiles());
        assertTrue(finalEntity.getUploadSuccess());

        verify(uploader).zipAndUploadFiles(exporterType.name().toLowerCase(), files);
    }

    @Test
    void testTrackExport_Success_WithFiles_UploadFailure() {
        // Arrange
        ExporterType exporterType = ExporterType.INFLUXDB;
        String exportName = "influxExport";
        String requestParams = "bucket=test";
        List<Path> files = List.of(Paths.get("data.csv"));
        Supplier<List<Path>> exporterCall = () -> files;
        doThrow(new RuntimeException("Upload failed")).when(uploader).zipAndUploadFiles(anyString(), anyList());

        // Act
        List<Path> result = exportTrackingService.trackExport(exporterType, exportName, requestParams, exporterCall);

        // Assert
        assertEquals(files, result);
        ArgumentCaptor<ExportRunEntity> captor = ArgumentCaptor.forClass(ExportRunEntity.class);
        verify(exportRunRepository, times(2)).save(captor.capture());
        List<ExportRunEntity> savedEntities = captor.getAllValues();

        ExportRunEntity finalEntity = savedEntities.get(1);
        assertEquals(Status.SUCCESS.name(), finalEntity.getStatus());
        assertFalse(finalEntity.getUploadSuccess());
        assertEquals("data.csv", finalEntity.getExportedFiles());
    }

    @Test
    void testTrackExport_Success_NoFiles() {
        // Arrange
        ExporterType exporterType = ExporterType.VOCABULARY;
        String exportName = "vocabExport";
        String requestParams = "";
        List<Path> files = List.of();
        Supplier<List<Path>> exporterCall = () -> files;

        // Act
        List<Path> result = exportTrackingService.trackExport(exporterType, exportName, requestParams, exporterCall);

        // Assert
        assertEquals(files, result);
        ArgumentCaptor<ExportRunEntity> captor = ArgumentCaptor.forClass(ExportRunEntity.class);
        verify(exportRunRepository, times(2)).save(captor.capture());
        List<ExportRunEntity> savedEntities = captor.getAllValues();

        ExportRunEntity finalEntity = savedEntities.get(1);
        assertEquals(Status.SUCCESS.name(), finalEntity.getStatus());
        assertNull(finalEntity.getExportedFiles());
        assertTrue(finalEntity.getUploadSuccess());
    }

    @Test
    void testTrackExport_Failure() {
        // Arrange
        ExporterType exporterType = ExporterType.COSTS;
        String exportName = "failedExport";
        String requestParams = "invalid";
        Supplier<List<Path>> exporterCall = () -> { throw new RuntimeException("Export error"); };

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            exportTrackingService.trackExport(exporterType, exportName, requestParams, exporterCall));

        assertEquals("Export error", exception.getMessage());

        ArgumentCaptor<ExportRunEntity> captor = ArgumentCaptor.forClass(ExportRunEntity.class);
        verify(exportRunRepository, times(2)).save(captor.capture());
        List<ExportRunEntity> savedEntities = captor.getAllValues();

        ExportRunEntity finalEntity = savedEntities.get(1);
        assertEquals(Status.FAILED.name(), finalEntity.getStatus());
        assertNotNull(finalEntity.getFinishedAt());
        assertNotNull(finalEntity.getDurationMs());
        assertEquals("Export error", finalEntity.getErrorMessage());
        assertNull(finalEntity.getUploadSuccess());
    }
}