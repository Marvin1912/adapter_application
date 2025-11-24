package com.marvin.app.controller;

import com.marvin.app.controller.dto.FileListResponse;
import com.marvin.upload.FileLister;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "File List", description = "API for listing files in Google Drive")
public class FileListController {

    private final FileLister fileLister;

    public FileListController(FileLister fileLister) {
        this.fileLister = fileLister;
    }

    @Operation(
        summary = "List files in Google Drive",
        description = "Retrieves a list of all files and folders in the configured Google Drive parent folder. The listing includes both files and subdirectories with their names."
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
            final List<String> files = fileLister.listFiles();
            return ResponseEntity.ok(FileListResponse.success("Successfully listed files from Google Drive", files));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(FileListResponse.error("Failed to list files from Google Drive: " + e.getMessage()));
        }
    }
}