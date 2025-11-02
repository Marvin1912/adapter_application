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
        final Path zipFilePath = dirPath.resolve(
                "files_" + LocalDateTime.now().format(FILE_DTF) + ".zip");

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(
                Files.newOutputStream(zipFilePath))) {
            zipOutputStream.setLevel(9);
            filesToZipAndUpload.stream()
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        Path filePath = dirPath.resolve(path);
                        ZipEntry zipEntry = new ZipEntry(filePath.toString());
                        LOGGER.info("Added zip entry {}", zipEntry.getName());
                        try {
                            zipOutputStream.putNextEntry(zipEntry);
                            Files.copy(filePath, zipOutputStream);
                            zipOutputStream.closeEntry();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException("Could not handle files!", e);
        }

        try {
            String parentId = googleDrive.getFileId(parentFolderName);
            googleDrive.uploadFile(zipFilePath, parentId);
            for (Path path : filesToZipAndUpload) {
                Path filePath = dirPath.resolve(path);
                try {
                    Files.delete(filePath);
                    LOGGER.info("Deleted file {}!", filePath);
                } catch (IOException e) {
                    LOGGER.error("Could not delete file {}!", filePath, e);
                }
            }
            try {
                Files.delete(zipFilePath);
                LOGGER.info("Deleted file {}!", zipFilePath);
            } catch (IOException e) {
                LOGGER.error("Could not delete file {}!", zipFilePath, e);
            }
        } catch (GoogleDriveException e) {
            LOGGER.error("Could not upload file!", e);
            throw new IllegalStateException(e);
        }
    }

}
