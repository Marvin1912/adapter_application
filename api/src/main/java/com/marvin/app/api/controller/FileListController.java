package com.marvin.app.api.controller;

import com.marvin.app.api.dto.FileListResponse;
import com.marvin.app.api.dto.FileDeleteResponse;
import com.marvin.upload.FileLister;
import com.marvin.upload.FileDeleter;
import com.marvin.upload.DriveFileInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "File List", description = "API for listing files in Google Drive")
public class FileListController {

    private final FileLister fileLister;
    private final FileDeleter fileDeleter;

    public FileListController(FileLister fileLister, FileDeleter fileDeleter) {
        this.fileLister = fileLister;
        this.fileDeleter = fileDeleter;
    }

    @Operation(
        summary = "List files in Google Drive",
        description = "Retrieves a list of all files and folders in the configured Google Drive parent folder. The listing includes file metadata such as ID, name, size, modification time, and web view link. File IDs can be used for deletion operations."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved file list from Google Drive",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = FileListResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error occurred while accessing Google Drive",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = FileListResponse.class)
            )
        )
    })
    @GetMapping(path = "/files/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FileListResponse> listFiles() {
        try {
            final List<DriveFileInfo> files = fileLister.listFiles();
            return ResponseEntity.ok(FileListResponse.success("Successfully listed files from Google Drive", files));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(FileListResponse.error("Failed to list files from Google Drive: " + e.getMessage()));
        }
    }

    @Operation(
        summary = "Delete a file from Google Drive",
        description = "Deletes a specific file from Google Drive using its file ID. This operation is permanent and cannot be undone."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "File successfully deleted from Google Drive",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = FileDeleteResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request - invalid file ID provided",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = FileDeleteResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "File not found in Google Drive",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = FileDeleteResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error occurred while deleting file from Google Drive",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = FileDeleteResponse.class)
            )
        )
    })
    @DeleteMapping(path = "/files/{fileId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FileDeleteResponse> deleteFile(
            @Parameter(description = "The ID of the file to delete from Google Drive", required = true)
            @PathVariable String fileId) {
        try {
            if (fileId == null || fileId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(FileDeleteResponse.error("File ID cannot be null or empty"));
            }

            fileDeleter.deleteFile(fileId);
            return ResponseEntity.ok(FileDeleteResponse.success("File successfully deleted from Google Drive", fileId));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(FileDeleteResponse.error("Failed to delete file from Google Drive: " + e.getMessage()));
        }
    }
}
