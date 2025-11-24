package com.marvin.upload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class FileLister {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileLister.class);

    private final String costExportFolder;

    public FileLister(@Value("${uploader.cost-export-folder}") String costExportFolder) {
        this.costExportFolder = costExportFolder;
    }

    public List<String> listFiles() {
        LOGGER.info("Listing files in folder: {}", costExportFolder);

        final File folder = new File(costExportFolder);

        if (!folder.exists()) {
            LOGGER.error("Folder does not exist: {}", costExportFolder);
            throw new IllegalArgumentException("Folder does not exist: " + costExportFolder);
        }

        if (!folder.isDirectory()) {
            LOGGER.error("Path is not a directory: {}", costExportFolder);
            throw new IllegalArgumentException("Path is not a directory: " + costExportFolder);
        }

        final List<String> fileNames = new ArrayList<>();
        final File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                fileNames.add(file.getName());
                LOGGER.debug("Found: {} ({})", file.getName(), file.isDirectory() ? "directory" : "file");
            }
        }

        LOGGER.info("Found {} items in folder: {}", fileNames.size(), costExportFolder);
        return fileNames;
    }
}