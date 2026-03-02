package com.study_companion.backend.controller.postgres;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.study_companion.backend.dto.UploadDto;
import com.study_companion.backend.exception.user.UnauthorizedUserAccessException;
import com.study_companion.backend.model.postgres.Upload;
import com.study_companion.backend.service.UploadService;
import com.study_companion.backend.util.SecurityUtils;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(value = "/api/upload")
@CrossOrigin
@Tag(name = "File Upload Management", description = "Operations for managing file uploads and parsing status")
public class UploadController {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private final UploadService uploadService;

    private final SecurityUtils securityUtils;

    UploadController(UploadService uploadService, SecurityUtils securityUtils) {
        this.uploadService = uploadService;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/getAllUploads")
    @Operation(summary = "Get all uploads", description = "Retrieve all file uploads in the system. Requires admin access.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Uploads retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - admin privileges required")
    })
    public List<Upload> getAllUploads(@Parameter(hidden = true) Authentication authentication) {
        if (!securityUtils.isAdmin(authentication)) {
            throw new UnauthorizedUserAccessException("Unauthorized user access");
        }
        return uploadService.getAllUploads();
    }

    @GetMapping("/{uploadId}")
    @Operation(summary = "Get upload by ID", description = "Retrieve a specific file upload by its UUID. Requires authentication or admin access.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Upload retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid upload ID format"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Upload not found")
    })
    public Upload getUploadById(
            @Parameter(description = "UUID of the upload to retrieve") @PathVariable UUID uploadId, @Parameter(hidden = true) Authentication authentication) {
                UUID requestingUserId = UUID.fromString(authentication.getName());
                if (!securityUtils.canAccess(authentication, requestingUserId)) {
            throw new UnauthorizedUserAccessException("Unauthorized user access");
        }
        return uploadService.getUploadById(uploadId);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user uploads", description = "Retrieve all file uploads belonging to a specific user. Requires authentication or admin access.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User uploads retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user ID format"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public List<Upload> getUserUploads(
            @Parameter(description = "UUID of the user whose uploads to retrieve") @PathVariable UUID userId, @Parameter(hidden = true) Authentication authentication) {
                if (!securityUtils.canAccess(authentication, userId)) {
            throw new UnauthorizedUserAccessException("Unauthorized user access");
        }
        return uploadService.getAllUserUploads(userId);
    }

    @GetMapping("/user/{userId}/count")
    @Operation(summary = "Count user uploads", description = "Get the total number of uploads belonging to a specific user. Requires authentication.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Upload count retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user ID format"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public long countUserUploads(
            @Parameter(description = "UUID of the user whose upload count to retrieve") @PathVariable UUID userId) {
        return uploadService.getCountOfAllUserUploads(userId);
    }

    @GetMapping("/deck/{deckId}")
    @Operation(summary = "Get deck uploads", description = "Retrieve all file uploads associated with a specific deck. Requires authentication or admin access.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deck uploads retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid deck ID format"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Deck not found")
    })
    public List<Upload> getDeckUploads(
            @Parameter(description = "UUID of the deck whose uploads to retrieve") @PathVariable UUID deckId) {
        return uploadService.getAllDeckUploads(deckId);
    }

    @GetMapping("/deck/{deckId}/count")
    @Operation(summary = "Count deck uploads", description = "Get the total number of uploads associated with a specific deck. Requires authentication.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Upload count retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid deck ID format"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Deck not found")
    })
    public long countDeckUploads(
            @Parameter(description = "UUID of the deck whose upload count to retrieve") @PathVariable UUID deckId) {
        return uploadService.getCountOfAllDeckUploads(deckId);
    }

    @PostMapping("/createUpload")
    @Operation(summary = "Create new upload", description = "Upload a file to be processed into flashcards. Maximum file size is 10MB. Only deck owners can upload files.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Upload created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data, empty file, or file too large (max 10MB)"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - not deck owner"),
            @ApiResponse(responseCode = "404", description = "Deck not found")
    })
    public ResponseEntity<Upload> createNewUpload(
            @Parameter(description = "Upload creation data containing deck ID and metadata") @RequestBody UploadDto.Create uploadDto,
            @Parameter(description = "File to upload (max 10MB)") @RequestParam MultipartFile file,
            @Parameter(hidden = true) Authentication authentication) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File too large");
        }

        UUID requestingUserId = UUID.fromString(authentication.getName());

        long fileSize = file.getSize();

        Upload newUpload = uploadService.createUpload(uploadDto, fileSize, requestingUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUpload);
    }

    @PutMapping("/{uploadId}/start")
    @Operation(summary = "Start upload parsing", description = "Mark an upload as started for processing. Only the upload owner can perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Upload parsing started successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid upload ID format or upload not in correct state"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - not upload owner"),
            @ApiResponse(responseCode = "404", description = "Upload not found")
    })
    public ResponseEntity<Upload> startParsingUpload(
            @Parameter(description = "UUID of the upload to start parsing") @PathVariable UUID uploadId, 
            @Parameter(hidden = true) Authentication authentication) {
        UUID requestingUserId = UUID.fromString(authentication.getName());
        Upload updatedUpload = uploadService.startParsingUpload(uploadId, requestingUserId);
        return ResponseEntity.ok(updatedUpload);
    }

    @PutMapping("/{uploadId}/complete")
    @Operation(summary = "Complete upload parsing", description = "Mark an upload as successfully completed. Only the upload owner can perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Upload parsing completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid upload ID format or upload not in correct state"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - not upload owner"),
            @ApiResponse(responseCode = "404", description = "Upload not found")
    })
    public ResponseEntity<Upload> completeParsingUpload(
            @Parameter(description = "UUID of the upload to complete") @PathVariable UUID uploadId, 
            @Parameter(hidden = true) Authentication authentication) {
        UUID requestingUserId = UUID.fromString(authentication.getName());
        Upload updatedUpload = uploadService.completeParsingUpload(uploadId, requestingUserId);
        return ResponseEntity.ok(updatedUpload);
    }

    @PutMapping("/{uploadId}/failed")
    @Operation(summary = "Mark upload parsing as failed", description = "Mark an upload as failed with an error message. Only the upload owner can perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Upload marked as failed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid upload ID format or upload not in correct state"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - not upload owner"),
            @ApiResponse(responseCode = "404", description = "Upload not found")
    })
    public ResponseEntity<Upload> failParsingUpload(
            @Parameter(description = "UUID of the upload to mark as failed") @PathVariable UUID uploadId, 
            @Parameter(description = "Error message describing the failure reason") @RequestBody String errorMessage,
            @Parameter(hidden = true) Authentication authentication) {
        UUID requestingUserId = UUID.fromString(authentication.getName());
        Upload updatedUpload = uploadService.failParsingUpload(uploadId, errorMessage, requestingUserId);
        return ResponseEntity.ok(updatedUpload);
    }

    @DeleteMapping("/delete/{uploadId}")
    @Operation(summary = "Delete upload", description = "Permanently delete a file upload record and associated data. Only the upload owner can perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Upload deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid upload ID format"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - not upload owner"),
            @ApiResponse(responseCode = "404", description = "Upload not found")
    })
    public ResponseEntity<String> deleteUploadById(
            @Parameter(description = "UUID of the upload to delete") @PathVariable UUID uploadId, 
            @Parameter(hidden = true) Authentication authentication) {
        UUID requestingUserId = UUID.fromString(authentication.getName());
        uploadService.deleteUploadById(uploadId, requestingUserId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete/user/{userId}")
    @Operation(summary = "Delete all user uploads", description = "Delete all uploads belonging to a specific user. Requires admin privileges.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User uploads deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user ID format"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - admin privileges required"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<String> deleteAllUserUploads(
            @Parameter(description = "UUID of the user whose uploads to delete") @PathVariable UUID userId,
            @Parameter(hidden = true) Authentication authentication) {
        if (!securityUtils.isAdmin(authentication)) {
            throw new UnauthorizedUserAccessException("Unauthorized user access");
        }
        uploadService.deleteAllUserUploads(userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete/deck/{deckId}")
    @Operation(summary = "Delete all deck uploads", description = "Delete all uploads associated with a specific deck. Requires admin privileges.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deck uploads deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid deck ID format"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - admin privileges required"),
            @ApiResponse(responseCode = "404", description = "Deck not found")
    })
    public ResponseEntity<String> deleteAllDeckUploads(
            @Parameter(description = "UUID of the deck whose uploads to delete") @PathVariable UUID deckId,
            @Parameter(hidden = true) Authentication authentication) {
        if (!securityUtils.isAdmin(authentication)) {
            throw new UnauthorizedUserAccessException("Unauthorized user access");
        }
        uploadService.deleteAllDeckUploads(deckId);
        return ResponseEntity.noContent().build();
    }
}
