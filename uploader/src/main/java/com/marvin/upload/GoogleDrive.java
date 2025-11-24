package com.marvin.upload;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GoogleDrive {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleDrive.class);

    private static final String APPLICATION_NAME = "Applications";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);

    private final String credentialsPath;

    public GoogleDrive(@Value("${uploader.credentials.path}") String credentialsPath) {
        this.credentialsPath = credentialsPath;
    }

    public String getFileId(String folderName) throws GoogleDriveException {
        LOGGER.info("Trying to get file ID for {}!", folderName);

        try {
            final Drive service = createDriveService();
            final FileList result = searchFolderByName(service, folderName);

            if (result == null || result.getFiles().isEmpty()) {
                throw new GoogleDriveException("No folder with name " + folderName + " found!");
            }

            return result.getFiles().get(0).getId();
        } catch (Exception e) {
            throw new GoogleDriveException(e);
        }
    }

    public void uploadFile(Path path, String parent) throws GoogleDriveException {
        LOGGER.info("Trying to upload file to {}/{} !", parent, path.getFileName());

        try {
            final Drive service = createDriveService();
            final File uploadedFile = performFileUpload(service, path, parent);
            LOGGER.info("Uploaded file {}. File ID: {}.", path.getFileName(), uploadedFile.getId());
        } catch (Exception e) {
            throw new GoogleDriveException(e);
        }
    }

    public List<DriveFileInfo> listFiles(String folderId) throws GoogleDriveException {
        LOGGER.info("Listing files in folder: {}", folderId);

        try {
            final Drive service = createDriveService();
            final List<File> files = new ArrayList<>();
            String pageToken = null;

            do {
                final FileList result = service.files().list()
                        .setQ("'" + folderId + "' in parents and trashed=false")
                        .setFields("nextPageToken, files(id, name, mimeType, size, modifiedTime, webViewLink)")
                        .setPageToken(pageToken)
                        .execute();

                files.addAll(result.getFiles());
                pageToken = result.getNextPageToken();
            } while (pageToken != null);

            LOGGER.info("Found {} files in folder: {}", files.size(), folderId);
            return files.stream()
                    .map(this::convertToDriveFileInfo)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new GoogleDriveException("Failed to list files in folder: " + folderId, e);
        }
    }

    public List<DriveFileInfo> listFiles(String folderId, String mimeType) throws GoogleDriveException {
        LOGGER.info("Listing files in folder: {} with mimeType: {}", folderId, mimeType);

        try {
            final Drive service = createDriveService();
            final List<File> files = new ArrayList<>();
            String pageToken = null;

            do {
                final FileList result = service.files().list()
                        .setQ("'" + folderId + "' in parents and trashed=false and mimeType='" + mimeType + "'")
                        .setFields("nextPageToken, files(id, name, mimeType, size, modifiedTime, webViewLink)")
                        .setPageToken(pageToken)
                        .execute();

                files.addAll(result.getFiles());
                pageToken = result.getNextPageToken();
            } while (pageToken != null);

            LOGGER.info("Found {} files in folder: {} with mimeType: {}", files.size(), folderId, mimeType);
            return files.stream()
                    .map(this::convertToDriveFileInfo)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new GoogleDriveException("Failed to list files in folder: " + folderId + " with mimeType: " + mimeType, e);
        }
    }

    private DriveFileInfo convertToDriveFileInfo(File file) {
        final Long size = file.getSize() != null ? file.getSize() : null;
        return new DriveFileInfo(
                file.getId(),
                file.getName(),
                file.getMimeType(),
                size,
                file.getModifiedTime(),
                file.getWebViewLink()
        );
    }

    private Drive createDriveService() throws Exception {
        final NetHttpTransport netHttpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new Drive.Builder(netHttpTransport, JSON_FACTORY, getCredentials())
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private FileList searchFolderByName(Drive service, String folderName) throws Exception {
        return service.files().list()
                .setQ("mimeType = 'application/vnd.google-apps.folder' and name = '" + folderName + "'")
                .setFields("files(id)")
                .execute();
    }

    private File performFileUpload(Drive service, Path path, String parent) throws Exception {
        final File fileMetadata = createFileMetadata(path, parent);
        final java.io.File filePath = path.toAbsolutePath().toFile();
        final FileContent mediaContent = new FileContent("application/zip", filePath);

        return service.files()
                .create(fileMetadata, mediaContent)
                .setFields("id")
                .execute();
    }

    private File createFileMetadata(Path path, String parent) {
        final File fileMetadata = new File();
        fileMetadata.setName(path.getFileName().toString());
        fileMetadata.setParents(List.of(parent));
        return fileMetadata;
    }

    private Credential getCredentials() throws IOException {
        return GoogleCredential
                .fromStream(new FileInputStream(credentialsPath))
                .createScoped(SCOPES);
    }
}
