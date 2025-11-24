package com.marvin.upload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class FileLister {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileLister.class);

    private final String parentFolderName;
    private final GoogleDrive googleDrive;

    public FileLister(
            @Value("${uploader.parent-folder-name}") String parentFolderName,
            GoogleDrive googleDrive
    ) {
        this.parentFolderName = parentFolderName;
        this.googleDrive = googleDrive;
    }

    public List<String> listFiles() {
        LOGGER.info("Listing files in Google Drive folder: {}", parentFolderName);

        try {
            final String folderId = googleDrive.getFileId(parentFolderName);
            final List<DriveFileInfo> driveFiles = googleDrive.listFiles(folderId);

            final List<String> fileNames = driveFiles.stream()
                    .map(DriveFileInfo::getName)
                    .collect(Collectors.toList());

            for (DriveFileInfo file : driveFiles) {
                LOGGER.debug("Found: {} ({})", file.getName(), file.isDirectory() ? "directory" : "file");
            }

            LOGGER.info("Found {} items in Google Drive folder: {}", fileNames.size(), parentFolderName);
            return fileNames;

        } catch (GoogleDriveException e) {
            LOGGER.error("Failed to list files in Google Drive folder: {}", parentFolderName, e);
            throw new RuntimeException("Failed to list files in Google Drive folder: " + parentFolderName, e);
        }
    }
}