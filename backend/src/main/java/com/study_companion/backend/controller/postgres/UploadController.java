package com.study_companion.backend.controller.postgres;

import java.util.List;
import java.util.UUID;

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
import com.study_companion.backend.model.postgres.Upload;
import com.study_companion.backend.service.UploadService;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping(value = "/api/upload")
@CrossOrigin 
public class UploadController {
    
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private final UploadService uploadService;

    UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    // FIXME: POST 201 with return
    // ResponseEntity.status(HttpStatus.CREATED).body(createdUser), PUT 200 with
    // return ResponseEntity.ok(updatedDeck), DELETE 204 with return
    // ResponseEntity.noContent().build()

    @GetMapping("/getAllUploads")
    public List<Upload> getAllUploads() {
        return uploadService.getAllUploads();
    }
    
    @GetMapping("/{uploadId}")
    public Upload getUploadById(@PathVariable UUID uploadId) {
        return uploadService.getUploadById(uploadId);
    }

    @GetMapping("/user/{userId}")
    public List<Upload> getUserUploads(@PathVariable UUID userId) {
        return uploadService.getAllUserUploads(userId);
    }

    @GetMapping("/user/{userId}/count")
    public long countUserUploads(@PathVariable UUID userId) {
        return uploadService.getCountOfAllUserUploads(userId);
    }

    @GetMapping("/deck/{deckId}")
    public List<Upload> getDeckUploads(@PathVariable UUID deckId) {
        return uploadService.getAllDeckUploads(deckId);
    }
    
    @GetMapping("/deck/{deckId}/count")
    public long countDeckUploads(@PathVariable UUID deckId) {
        return uploadService.getCountOfAllDeckUploads(deckId);
    }
    
    @PostMapping("/createUpload")
    public ResponseEntity<String> createNewUpload(@RequestBody UploadDto.Create uploadDto, @RequestParam MultipartFile file,
                                   Authentication authentication) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File cannot be empty");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.badRequest().body("File too large");
        }

        UUID requestingUserId = UUID.fromString(authentication.getName());

        long fileSize = file.getSize();

        uploadService.createUpload(uploadDto, fileSize, requestingUserId);
        return ResponseEntity.ok("Successfully created new upload");
    }

    @PutMapping("/{uploadId}/start")
    public ResponseEntity<String> startParsingUpload(@PathVariable UUID uploadId, Authentication authentication) {
        UUID requestingUserId = UUID.fromString(authentication.getName());
        uploadService.startParsingUpload(uploadId, requestingUserId);
        return ResponseEntity.ok("Successfully started parsing");
    }

    @PutMapping("/{uploadId}/complete")
    public ResponseEntity<String> completeParsingUpload(@PathVariable UUID uploadId, Authentication authentication) {
        UUID requestingUserId = UUID.fromString(authentication.getName());
        uploadService.completeParsingUpload(uploadId, requestingUserId); 
        return ResponseEntity.ok("Successfully completed parsing");
    }

    @PutMapping("/{uploadId}/failed")
    public ResponseEntity<String> failParsingUpload(@PathVariable UUID uploadId, @RequestBody String errorMessage, Authentication authentication) {
        UUID requestingUserId = UUID.fromString(authentication.getName()); 
        uploadService.failParsingUpload(uploadId, errorMessage, requestingUserId);
        return ResponseEntity.ok("Saved failed parsing job");
    }

    @DeleteMapping("/delete/{uploadId}")
    public ResponseEntity<String> deleteUploadById(@PathVariable UUID uploadId, Authentication authentication) {
        UUID requestingUserId = UUID.fromString(authentication.getName()); 
        uploadService.deleteUploadById(uploadId, requestingUserId);
        return ResponseEntity.ok("Successfully deleted upload");
    }

    @DeleteMapping("/delete/user/{userId}")
    public ResponseEntity<String> deleteAllUserUploads(@PathVariable UUID userId) {
        uploadService.deleteAllUserUploads(userId);
        return ResponseEntity.ok("Successfully deleted all user uploads");
    }

    @DeleteMapping("/delete/deck/{deckId}")
    public ResponseEntity<String> deleteAllDeckUploads(@PathVariable UUID deckId) {
        uploadService.deleteAllDeckUploads(deckId);
        return ResponseEntity.ok("Successfully deleted all deck uploads");
    }
}
