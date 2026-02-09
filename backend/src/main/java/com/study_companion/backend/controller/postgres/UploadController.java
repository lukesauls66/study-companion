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

    @GetMapping("path")
    public String getMethodName(@RequestParam String param) {
        return new String();
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
}
