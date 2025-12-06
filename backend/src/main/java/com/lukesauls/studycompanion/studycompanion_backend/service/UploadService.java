package com.lukesauls.studycompanion.studycompanion_backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.lukesauls.studycompanion.studycompanion_backend.dto.UploadDto;
import com.lukesauls.studycompanion.studycompanion_backend.exception.InvalidUploadCreationException;
import com.lukesauls.studycompanion.studycompanion_backend.exception.UnauthorizedUploadAccessException;
import com.lukesauls.studycompanion.studycompanion_backend.exception.UploadNotFoundException;
import com.lukesauls.studycompanion.studycompanion_backend.exception.UnauthorizedDeckAccessException;
import com.lukesauls.studycompanion.studycompanion_backend.model.postgres.Deck;
import com.lukesauls.studycompanion.studycompanion_backend.model.postgres.Upload;
import com.lukesauls.studycompanion.studycompanion_backend.model.postgres.User;
import com.lukesauls.studycompanion.studycompanion_backend.repository.postgres.UploadRepository;

@Service
public class UploadService {

    @Autowired
    private UploadRepository uploadRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private DeckService deckService;

    /**
     * Creates a new upload record for the specified deck.
     * Only the deck owner can upload files to their deck.
     * Validates that fileName is not empty and establishes bidirectional relationships.
     * 
     * @param uploadDto the upload creation data containing userId, deckId, fileName, and fileType
     * @param fileUrl the URL where the uploaded file is stored
     * @param fileSize the size of the uploaded file in bytes
     * @return the created upload with generated ID and timestamps
     * @throws InvalidUploadCreationException if fileName is blank or fileUrl is blank or fileSize is invalid
     * @throws UnauthorizedDeckAccessException if the user is not the deck owner
     * @throws UserNotFoundException if the specified user does not exist
     * @throws DeckNotFoundException if the specified deck does not exist
     */
    @SuppressWarnings("null")
    public @NonNull Upload createUpload(@NonNull UploadDto.Create uploadDto, @NonNull String fileUrl, @NonNull Long fileSize) {
        User user = userService.getUserById(uploadDto.userId());
        Deck deck = deckService.getDeckById(uploadDto.deckId());

        if (!deck.getUser().getId().equals(uploadDto.userId())) {
            throw new UnauthorizedDeckAccessException("You can only upload files to your own decks");
        }

        if (uploadDto.fileName().trim().isEmpty()) {
            throw new InvalidUploadCreationException("File name cannot be empty");
        }

        if (fileUrl.trim().isEmpty()) {
            throw new InvalidUploadCreationException("File URL cannot be empty");
        }

        if (fileSize <= 0) {
            throw new InvalidUploadCreationException("File size must be greater than 0");
        }

        Upload upload = new Upload(user, deck, uploadDto.fileName().trim(), fileUrl.trim(), uploadDto.fileType(), fileSize);
        
        user.addUpload(upload);
        deck.addUpload(upload);

        return uploadRepository.save(upload);
    }

    /**
     * Retrieves an upload by its unique identifier.
     * 
     * @param id the UUID of the upload to retrieve
     * @return the upload with the specified ID
     * @throws UploadNotFoundException if no upload exists with the given ID
     */
    @SuppressWarnings("null")
    public @NonNull Upload getUploadById(@NonNull UUID id) {
        if (!uploadRepository.existsById(id)) {
            throw new UploadNotFoundException("Upload with ID " + id + " not found");
        }

        return uploadRepository.findById(id).get();
    }

    /**
     * Retrieves all uploads in the system.
     * Currently unrestricted - should be limited to admin users in production.
     * 
     * @return a list of all uploads in the system
     */
    //FIXME: Add requestUUID and only fetch if UUID belongs to an admin
    public List<Upload> getAllUploads() {
        return uploadRepository.findAll();
    }

    /**
     * Retrieves all uploads belonging to a specific user.
     * 
     * @param userId the UUID of the user whose uploads to retrieve
     * @return a list of uploads owned by the user, empty if user has no uploads
     */
    public List<Upload> getAllUserUploads(@NonNull UUID userId) {
        return uploadRepository.findByUserId(userId);
    }

    /**
     * Retrieves all uploads belonging to a specific deck.
     * 
     * @param deckId the UUID of the deck whose uploads to retrieve
     * @return a list of uploads belonging to the deck, empty if deck has no uploads
     */
    public List<Upload> getAllDeckUploads(@NonNull UUID deckId) {
        return uploadRepository.findByDeckId(deckId);
    }

    /**
     * Counts the total number of uploads belonging to a specific user.
     * 
     * @param userId the UUID of the user whose upload count to retrieve
     * @return the number of uploads owned by the user
     */
    public long getCountOfAllUserUploads(@NonNull UUID userId) {
        return uploadRepository.countByUserId(userId);
    }

    /**
     * Counts the total number of uploads belonging to a specific deck.
     * 
     * @param deckId the UUID of the deck whose upload count to retrieve
     * @return the number of uploads belonging to the deck
     */
    public long getCountOfAllDeckUploads(@NonNull UUID deckId) {
        return uploadRepository.countByDeckId(deckId);
    }

    /**
     * Marks an upload as started for parsing.
     * Only the upload owner can perform this operation.
     * Updates parsing status to PROCESSING and sets parsing start time.
     * 
     * @param uploadId the UUID of the upload to start parsing
     * @param requestingUserId the UUID of the user making the request
     * @return the updated upload
     * @throws UploadNotFoundException if the upload does not exist
     * @throws UnauthorizedUploadAccessException if the requesting user is not the upload owner
     */
    public Upload startParsingUpload(@NonNull UUID uploadId, @NonNull UUID requestingUserId) {
        Upload upload = getUploadById(uploadId);

        if (!upload.getUser().getId().equals(requestingUserId)) {
            throw new UnauthorizedUploadAccessException("You can only modify your own uploads");
        }

        upload.startParsing();
        return uploadRepository.save(upload);
    }

    /**
     * Marks an upload as successfully parsed.
     * Only the upload owner can perform this operation.
     * Updates parsing status to COMPLETED and sets completion time.
     * 
     * @param uploadId the UUID of the upload to mark as completed
     * @param requestingUserId the UUID of the user making the request
     * @return the updated upload
     * @throws UploadNotFoundException if the upload does not exist
     * @throws UnauthorizedUploadAccessException if the requesting user is not the upload owner
     */
    public Upload completeParsingUpload(@NonNull UUID uploadId, @NonNull UUID requestingUserId) {
        Upload upload = getUploadById(uploadId);

        if (!upload.getUser().getId().equals(requestingUserId)) {
            throw new UnauthorizedUploadAccessException("You can only modify your own uploads");
        }

        upload.completeParsing();
        return uploadRepository.save(upload);
    }

    /**
     * Marks an upload as failed during parsing.
     * Only the upload owner can perform this operation.
     * Updates parsing status to FAILED, sets completion time, and stores error message.
     * 
     * @param uploadId the UUID of the upload to mark as failed
     * @param errorMessage the error message describing why parsing failed
     * @param requestingUserId the UUID of the user making the request
     * @return the updated upload
     * @throws UploadNotFoundException if the upload does not exist
     * @throws UnauthorizedUploadAccessException if the requesting user is not the upload owner
     */
    public Upload failParsingUpload(@NonNull UUID uploadId, @NonNull String errorMessage, @NonNull UUID requestingUserId) {
        Upload upload = getUploadById(uploadId);

        if (!upload.getUser().getId().equals(requestingUserId)) {
            throw new UnauthorizedUploadAccessException("You can only modify your own uploads");
        }

        upload.failParsing(errorMessage);
        return uploadRepository.save(upload);
    }

    /**
     * Deletes an upload by its unique identifier.
     * Only the upload owner can perform this operation.
     * Maintains bidirectional relationships by removing upload from both user's and deck's collections.
     * 
     * @param uploadId the UUID of the upload to delete
     * @param requestingUserId the UUID of the user making the request
     * @throws UploadNotFoundException if the upload does not exist
     * @throws UnauthorizedUploadAccessException if the requesting user is not the upload owner
     */
    public void deleteUploadById(@NonNull UUID uploadId, @NonNull UUID requestingUserId) {
        Upload upload = getUploadById(uploadId);

        if (!upload.getUser().getId().equals(requestingUserId)) {
            throw new UnauthorizedUploadAccessException("You can only delete your own uploads");
        }

        User user = upload.getUser();
        Deck deck = upload.getDeck();
        
        user.removeUpload(upload);
        deck.removeUpload(upload);

        uploadRepository.deleteById(uploadId);
    }

    /**
     * Deletes all uploads belonging to a specific user.
     * This is typically used for administrative purposes or account deletion.
     * Maintains bidirectional relationships by removing uploads from both user's and decks' collections.
     * Currently unrestricted - should be limited to admin users in production.
     * 
     * @param userId the UUID of the user whose uploads to delete
     * @throws UserNotFoundException if the specified user does not exist
     */
    //FIXME: Add requestUUID and only delete if UUID belongs to an admin
    public void deleteAllUserUploads(@NonNull UUID userId) {
        User user = userService.getUserById(userId);
        
        List<Upload> uploads = uploadRepository.findByUserId(userId);
        for (Upload upload : uploads) {
            Deck deck = upload.getDeck();
            deck.removeUpload(upload);
        }
        
        user.getUploads().clear();
        
        uploadRepository.deleteByUserId(userId);
    }

    /**
     * Deletes all uploads belonging to a specific deck.
     * This is typically used when a deck is deleted or for cleanup purposes.
     * Maintains bidirectional relationships by removing uploads from both deck's and users' collections.
     * Currently unrestricted - should be limited to admin users in production.
     * 
     * @param deckId the UUID of the deck whose uploads to delete
     * @throws DeckNotFoundException if the specified deck does not exist
     */
    //FIXME: Add requestUUID and only delete if UUID belongs to an admin
    public void deleteAllDeckUploads(@NonNull UUID deckId) {
        Deck deck = deckService.getDeckById(deckId);
        
        List<Upload> uploads = uploadRepository.findByDeckId(deckId);
        for (Upload upload : uploads) {
            User user = upload.getUser();
            user.removeUpload(upload);
        }
        
        deck.getUploads().clear();
        
        uploadRepository.deleteByDeckId(deckId);
    }
}
