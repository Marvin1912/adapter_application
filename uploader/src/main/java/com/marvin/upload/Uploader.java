package com.marvin.upload;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Uploader {

    private static final Logger LOGGER = LoggerFactory.getLogger(Uploader.class);

    private static final DateTimeFormatter FILE_DTF = DateTimeFormatter.ofPattern(
            "yyyyMMdd_hhmmss");

    private final String costExportFolder;
    private final String parentFolderName;
    private final GoogleDrive googleDrive;

    public Uploader(
            @Value("${uploader.cost-export-folder}") String costExportFolder,
            @Value("${uploader.parent-folder-name}") String parentFolderName,
            GoogleDrive googleDrive
    ) {
        this.costExportFolder = costExportFolder;
        this.parentFolderName = parentFolderName;
        this.googleDrive = googleDrive;
    }

    public void zipAndUploadCostFiles(List<Path> filesToZipAndUpload) {
        LOGGER.info("Going to zip and upload files!");

        final Path dirPath = Paths.get(costExportFolder);
        final Path zipFilePath = generateZipFilePath(dirPath);

        createZipFile(filesToZipAndUpload, dirPath, zipFilePath);
        uploadToGoogleDrive(zipFilePath);
        cleanupFiles(filesToZipAndUpload, dirPath, zipFilePath);
    }

    private Path generateZipFilePath(Path dirPath) {
        final String timestamp = LocalDateTime.now().format(FILE_DTF);
        return dirPath.resolve("files_" + timestamp + ".zip");
    }

    private void createZipFile(List<Path> filesToZipAndUpload, Path dirPath, Path zipFilePath) {
        try (final ZipOutputStream zipOutputStream = new ZipOutputStream(
                Files.newOutputStream(zipFilePath))) {
            zipOutputStream.setLevel(9);
            filesToZipAndUpload.stream()
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> addFileToZip(path, dirPath, zipOutputStream));
        } catch (IOException e) {
            throw new RuntimeException("Could not create zip file!", e);
        }
    }

    private void addFileToZip(Path path, Path dirPath, ZipOutputStream zipOutputStream) {
        final Path filePath = dirPath.resolve(path);
        final ZipEntry zipEntry = new ZipEntry(filePath.toString());
        LOGGER.info("Added zip entry {}", zipEntry.getName());

        try {
            zipOutputStream.putNextEntry(zipEntry);
            Files.copy(filePath, zipOutputStream);
            zipOutputStream.closeEntry();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void uploadToGoogleDrive(Path zipFilePath) {
        try {
            final String parentId = googleDrive.getFileId(parentFolderName);
            googleDrive.uploadFile(zipFilePath, parentId);
        } catch (GoogleDriveException e) {
            LOGGER.error("Could not upload file!", e);
            throw new IllegalStateException("Failed to upload zip file to Google Drive", e);
        }
    }

    private void cleanupFiles(List<Path> filesToZipAndUpload, Path dirPath, Path zipFilePath) {
        deleteUploadedFiles(filesToZipAndUpload, dirPath);
        deleteZipFile(zipFilePath);
    }

    private void deleteUploadedFiles(List<Path> filesToZipAndUpload, Path dirPath) {
        for (Path path : filesToZipAndUpload) {
            final Path filePath = dirPath.resolve(path);
            try {
                Files.delete(filePath);
                LOGGER.info("Deleted file {}!", filePath);
            } catch (IOException e) {
                LOGGER.error("Could not delete file {}!", filePath, e);
            }
        }
    }

    private void deleteZipFile(Path zipFilePath) {
        try {
            Files.delete(zipFilePath);
            LOGGER.info("Deleted file {}!", zipFilePath);
        } catch (IOException e) {
            LOGGER.error("Could not delete file {}!", zipFilePath, e);
        }
    }
}