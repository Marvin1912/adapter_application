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

    public String getFileId(String fileName) throws GoogleDriveException {

        LOGGER.info("Trying to get file ID for {}!", fileName);

        try {
            final NetHttpTransport netHttpTransport = GoogleNetHttpTransport.newTrustedTransport();
            final Drive service = new Drive.Builder(netHttpTransport, JSON_FACTORY,
                    getCredentialsPath())
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            final FileList result = service.files().list()
                    .setQ("mimeType = 'application/vnd.google-apps.folder' and name = 'backup'")
                    .setFields("files(id)")
                    .execute();

            if (result == null || result.isEmpty()) {
                throw new GoogleDriveException("No file with name " + fileName + " found!");
            }

            return result.getFiles().get(0).getId();
        } catch (Exception e) {
            throw new GoogleDriveException(e);
        }
    }

    public void uploadFile(Path path, String parent) throws GoogleDriveException {

        LOGGER.info("Trying to upload file to {}/{} !", parent, path.getFileName());

        try {
            final NetHttpTransport netHttpTransport = GoogleNetHttpTransport.newTrustedTransport();
            final Drive service = new Drive.Builder(netHttpTransport, JSON_FACTORY,
                    getCredentialsPath())
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            final File fileMetadata = new File();
            fileMetadata.setName(path.getFileName().toString());
            fileMetadata.setParents(List.of(parent));

            final var filePath = new java.io.File(path.toAbsolutePath().toString());
            final FileContent mediaContent = new FileContent("application/zip", filePath);

            final File file = service.files()
                    .create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();
            LOGGER.info("Uploaded file {}. File ID: {}.", path.getFileName(), file.getId());
        } catch (Exception e) {
            throw new GoogleDriveException(e);
        }
    }

    private Credential getCredentialsPath() throws IOException {
        return GoogleCredential
                .fromStream(new FileInputStream(credentialsPath))
                .createScoped(SCOPES);
    }

}
