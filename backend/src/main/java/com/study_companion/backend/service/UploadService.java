package com.study_companion.backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.study_companion.backend.dto.UploadDto;
import com.study_companion.backend.exception.upload.InvalidUploadCreationException;
import com.study_companion.backend.exception.upload.InvalidUploadParameterException;
import com.study_companion.backend.exception.upload.UnauthorizedUploadAccessException;
import com.study_companion.backend.exception.upload.UploadException;
import com.study_companion.backend.exception.upload.UploadNotFoundException;
import com.study_companion.backend.exception.upload.UploadOperationException;
import com.study_companion.backend.exception.user.UnauthorizedUserAccessException;
import com.study_companion.backend.exception.user.UserException;
import com.study_companion.backend.exception.user.UserNotFoundException;
import com.study_companion.backend.exception.user.UserOperationException;
import com.study_companion.backend.exception.deck.DeckException;
import com.study_companion.backend.exception.deck.DeckNotFoundException;
import com.study_companion.backend.exception.deck.DeckOperationException;
import com.study_companion.backend.exception.deck.UnauthorizedDeckAccessException;
import com.study_companion.backend.model.postgres.Deck;
import com.study_companion.backend.model.postgres.Upload;
import com.study_companion.backend.model.postgres.User;
import com.study_companion.backend.repository.postgres.DeckRepository;
import com.study_companion.backend.repository.postgres.UploadRepository;
import com.study_companion.backend.repository.postgres.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UploadService {

    private static final Logger logger = LoggerFactory.getLogger(UploadService.class);

    private final UploadRepository uploadRepository;

    private final UserRepository userRepository;

    private final DeckRepository deckRepository;

    private final DeckService deckService;

    UploadService(UploadRepository uploadRepository, UserRepository userRepository, DeckRepository deckRepository,
            DeckService deckService) {
        this.uploadRepository = uploadRepository;
        this.userRepository = userRepository;
        this.deckRepository = deckRepository;
        this.deckService = deckService;
    }

    /**
     * Creates a new upload record for the specified deck.
     * Only the deck owner can upload files to their deck.
     * Validates that fileName is not empty and establishes bidirectional
     * relationships.
     * 
     * @param uploadDto        the upload creation data containing userId, deckId,
     *                         fileName, and fileType
     * @param fileUrl          the URL where the uploaded file is stored
     * @param fileSize         the size of the uploaded file in bytes
     * @param requestingUserId the id belonging to the user making the request
     * @return the created upload with generated ID and timestamps
     * @throws InvalidUploadParameterException   if any nonnull arg is null
     * @throws InvalidUploadCreationException    if fileName is blank or fileUrl is
     *                                           blank or fileSize is invalid
     * @throws UnauthorizedDeckAccessException   if the user is not the deck owner
     * @throws UnauthorizedUploadAccessException if the userId associated with the
     *                                           uploadDto does not match the
     *                                           requestingUserId
     * @throws UserNotFoundException             if the specified user does not
     *                                           exist
     * @throws DeckNotFoundException             if the specified deck does not
     *                                           exist
     * @throws DeckOperationException            if deck operations fail
     * @throws UserOperationException            if user operations fail
     * @throws UploadOperationException          if server error occurs
     */
    public UploadDto.GetResponse createUpload(UploadDto.CreateRequest uploadDto,
            Long fileSize, UUID requestingUserId) {
        if (uploadDto == null) {
            throw new InvalidUploadParameterException("Upload data transfer object cannot be null");
        }

        if (fileSize == null) {
            throw new InvalidUploadParameterException("File size cannot be null");
        }

        if (requestingUserId == null) {
            throw new InvalidUploadParameterException("Requesting userId cannot be null");
        }

        if (uploadDto.fileName().trim().isEmpty()) {
            throw new InvalidUploadCreationException("File name cannot be empty");
        }

        if (uploadDto.fileUrl().trim().isEmpty()) {
            throw new InvalidUploadCreationException("File URL cannot be empty");
        }

        if (fileSize <= 0) {
            throw new InvalidUploadCreationException("File size must be greater than 0");
        }

        try {
            logger.debug("Checking if requestingUserId matches the userId from the uploadDto");
            if (!requestingUserId.equals(uploadDto.userId())) {
                throw new UnauthorizedUploadAccessException("Unauthorized user upload");
            }

            User user = userRepository.findById(uploadDto.userId())
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
            Deck deck = deckRepository.findById(uploadDto.deckId())
                    .orElseThrow(() -> new DeckNotFoundException("Deck with ID " + uploadDto.deckId() + " not found"));

            logger.debug("Verifying that the requesting user can upload files to this deck");
            if (!deck.getUser().getId().equals(uploadDto.userId())) {
                throw new UnauthorizedDeckAccessException("You can only upload files to your own decks");
            }

            logger.debug("Creating user");
            Upload upload = new Upload(user, deck, uploadDto.fileName().trim(), uploadDto.fileUrl().trim(),
                    uploadDto.fileType(),
                    fileSize);

            logger.debug("Saving new upload to database");
            Upload newUpload = uploadRepository.save(upload);
            logger.info("Successfully created new upload");

            logger.debug("Adding upload to parent user");
            user.addUpload(upload);
            logger.debug("Adding upload to parent deck");
            deck.addUpload(upload);
            logger.info("Successfully added upload to parents");

            return convertToDto(newUpload);
        } catch (UserException | DeckException e) {
            logger.error("Upload creation failed: {}", e.getMessage());
            throw e;
        } catch (UploadException e) {
            logger.error("Upload operation failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to create upload: {}", e.getMessage());
            throw new UploadOperationException("Failed to create upload", e);
        }
    }

    /**
     * Retrieves an upload by its unique identifier.
     * 
     * @param id the UUID of the upload to retrieve
     * @return the upload with the specified ID
     * @throws InvalidUploadParameterException if any nonnull arg is null
     * @throws UploadNotFoundException         if no upload exists with the given ID
     * @throws UploadOperationException        if server error occurs
     */
    public UploadDto.GetResponse getUploadById(UUID id) {
        if (id == null) {
            throw new InvalidUploadParameterException("id cannot be null");
        }

        try {
            logger.debug("Checking if upload exists by provided id");
            if (!uploadRepository.existsById(id)) {
                throw new UploadNotFoundException("Upload with ID " + id + " not found");
            }

            logger.debug("Fetching upload");
            UploadDto.GetResponse upload = convertToDto(uploadRepository.findById(id).get());
            logger.info("Successfully fetched upload");

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null) {
                throw new UnauthorizedDeckAccessException("Authentication required");
            }

            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            logger.debug("Checking if user is admin");
            if (!isAdmin && !upload.userId().equals(UUID.fromString(authentication.getName()))) {
                throw new UnauthorizedDeckAccessException("Unauthorized user access");
            }

            return upload;
        } catch (UploadException e) {
            logger.error("Upload fetch failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to fetch upload by provided id: {}", e.getMessage());
            throw new UploadOperationException("Failed to fetch upload by provided id", e);
        }
    }

    /**
     * Retrieves all uploads in the system.
     * Currently unrestricted - should be limited to admin users in production.
     * 
     * @return a list of all uploads in the system
     * @throws UnauthorizedUploadAccessException if requesting user is not an admin
     * @throws UploadOperationException          if server error occurs
     */
    public List<UploadDto.GetResponse> getAllUploads() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null) {
                throw new UnauthorizedUploadAccessException("Authentication required");
            }

            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            logger.debug("Checking if user is admin");
            if (!isAdmin) {
                throw new UnauthorizedUploadAccessException("Unauthorized user access");
            }

            logger.debug("Fetching all uploads");
            List<UploadDto.GetResponse> uploads = uploadRepository.findAll().stream().map(this::convertToDto).toList();
            logger.info("Successfully fetched all uploads");
            return uploads;
        } catch (UploadException e) {
            logger.error("Upload operation failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to fetch all uploads: {}", e.getMessage());
            throw new UploadOperationException("Failed to fetch all uploads", e);
        }
    }

    /**
     * Retrieves all uploads belonging to a specific user.
     * 
     * @param userId the UUID of the user whose uploads to retrieve
     * @return a list of uploads owned by the user, empty if user has no uploads
     * @throws InvalidUploadParameterException if any nonnull arg is null
     * @throws UploadOperationException        if server error occurs
     */
    public List<UploadDto.GetResponse> getAllUserUploads(UUID userId) {
        if (userId == null) {
            throw new InvalidUploadParameterException("userId cannot be null");
        }

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null) {
                throw new UnauthorizedDeckAccessException("Authentication required");
            }

            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            logger.debug("Checking if user is authorized");
            if (!isAdmin && !userId.equals(UUID.fromString(authentication.getName()))) {
                throw new UnauthorizedDeckAccessException("Unauthorized user access");
            }

            logger.debug("Fetching all uploads belonging to the provided user");
            List<UploadDto.GetResponse> userUploads = uploadRepository.findByUserId(userId).stream()
                    .map(this::convertToDto).toList();
            logger.info("Successfully fetched all uploads belonging to the provided user");
            return userUploads;
        } catch (Exception e) {
            logger.error("Failed to fetch uploads: {}", e.getMessage());
            throw new UploadOperationException("Failed to fetch uploads", e);
        }
    }

    /**
     * Retrieves all uploads belonging to a specific deck.
     * 
     * @param deckId the UUID of the deck whose uploads to retrieve
     * @return a list of uploads belonging to the deck, empty if deck has no uploads
     * @throws InvalidUploadParameterException if any nonnull arg is null
     * @throws UploadOperationException        if server error occurs
     */
    public List<UploadDto.GetResponse> getAllDeckUploads(UUID deckId) {
        if (deckId == null) {
            throw new InvalidUploadParameterException("deckId cannot be null");
        }

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null) {
                throw new UnauthorizedUserAccessException("Authentication required");
            }

            Deck deck = deckRepository.findById(deckId).orElseThrow(() -> new DeckNotFoundException("Deck not found"));

            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            logger.debug("Checking if user is admin or upload owner");
            if (!isAdmin && !deck.getUser().getId().equals(UUID.fromString(authentication.getName()))) {
                throw new UnauthorizedUserAccessException("Unauthorized user access");
            }

            logger.debug("Fetching all uploads belonging to the provided deck");
            List<UploadDto.GetResponse> uploads = uploadRepository.findByDeckId(deckId).stream().map(this::convertToDto)
                    .toList();
            logger.info("Successfully fetched all uploads belonging to the provided deck");

            return uploads;
        } catch (Exception e) {
            logger.error("Failed to fetch all uploads belonging to the provided deck: {}", e.getMessage());
            throw new UploadOperationException("Failed to fetch all uploads belonging to the provided deck", e);
        }
    }

    /**
     * Counts the total number of uploads belonging to a specific user.
     * 
     * @param userId the UUID of the user whose upload count to retrieve
     * @return the number of uploads owned by the user
     * @throws InvalidUploadParameterException if any nonnull arg is null
     * @throws UploadOperationException        if server error occurs
     */
    public Long getCountOfAllUserUploads(UUID userId) {
        if (userId == null) {
            throw new InvalidUploadParameterException("userId cannot be null");
        }

        try {
            logger.debug("Calculating number of uploads user has");
            long uploadCount = uploadRepository.countByUserId(userId);
            logger.info("Successfully calculated number of uploads user has");
            return uploadCount;
        } catch (Exception e) {
            logger.error("Failed to calculate number of uploads user has: {}", e.getMessage());
            throw new UploadOperationException("Failed to calculate number of uploads user has", e);
        }
    }

    /**
     * Counts the total number of uploads belonging to a specific deck.
     * 
     * @param deckId the UUID of the deck whose upload count to retrieve
     * @return the number of uploads belonging to the deck
     * @throws InvalidUploadParameterException if any nonnull arg is null
     * @throws UploadOperationException        if server error occurs
     */
    public Long getCountOfAllDeckUploads(UUID deckId) {
        if (deckId == null) {
            throw new InvalidUploadParameterException("deckId cannot be null");
        }

        try {
            logger.debug("Calculating number of uploads belonging to the provided deckId");
            long uploadCount = uploadRepository.countByDeckId(deckId);
            logger.info("Successfully calculated {} uploads belonging to the provided deckId", uploadCount);
            return uploadCount;
        } catch (Exception e) {
            logger.error("Failed to calculate number of uploads belonging to the provided deckId: {}", e.getMessage());
            throw new UploadOperationException("Failed to calculate number of uploads belonging to the provided deckId",
                    e);
        }
    }

    /**
     * Marks an upload as started for parsing.
     * Only the upload owner can perform this operation.
     * Updates parsing status to PROCESSING and sets parsing start time.
     * 
     * @param uploadId         the UUID of the upload to start parsing
     * @param requestingUserId the UUID of the user making the request
     * @return the updated upload
     * @throws InvalidUploadParameterException   if any nonnull arg is null
     * @throws UploadNotFoundException           if the upload does not exist
     * @throws UnauthorizedUploadAccessException if the requesting user is not the
     *                                           upload owner
     * @throws UploadOperationException          if server error occurs
     */
    public UploadDto.GetResponse startParsingUpload(UUID uploadId, UUID requestingUserId) {
        if (uploadId == null) {
            throw new InvalidUploadParameterException("uploadId cannot be null");
        }

        if (requestingUserId == null) {
            throw new InvalidUploadParameterException("Requesting userId cannot be null");
        }

        try {
            Upload upload = uploadRepository.findById(uploadId)
                    .orElseThrow(() -> new UploadNotFoundException("Upload not found"));

            logger.debug("Verifying requesting user is authorized to perform this action");
            if (!upload.getUser().getId().equals(requestingUserId)) {
                throw new UnauthorizedUploadAccessException("You can only modify your own uploads");
            }

            logger.debug("Starting parsing process");
            upload.startParsing();
            Upload updatedUpload = uploadRepository.save(upload);
            logger.info("Successfully started parsing process");
            return convertToDto(updatedUpload);
        } catch (UploadException e) {
            logger.error("Parsing upload failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to start parsing upload: {}", e.getMessage());
            throw new UploadOperationException("Failed to start parsing upload", e);
        }
    }

    /**
     * Marks an upload as successfully parsed.
     * Only the upload owner can perform this operation.
     * Updates parsing status to COMPLETED and sets completion time.
     * 
     * @param uploadId         the UUID of the upload to mark as completed
     * @param requestingUserId the UUID of the user making the request
     * @return the updated upload
     * @throws InvalidUploadParameterException   if any nonnull arg is null
     * @throws UploadNotFoundException           if the upload does not exist
     * @throws UnauthorizedUploadAccessException if the requesting user is not the
     *                                           upload owner
     * @throws UploadOperationException          if server error occurs
     */
    public UploadDto.GetResponse completeParsingUpload(UUID uploadId, UUID requestingUserId) {
        if (uploadId == null) {
            throw new InvalidUploadParameterException("uploadId cannot be null");
        }

        if (requestingUserId == null) {
            throw new InvalidUploadParameterException("Requesting userId cannot be null");
        }

        try {
            Upload upload = uploadRepository.findById(uploadId)
                    .orElseThrow(() -> new UploadNotFoundException("Upload not found"));

            logger.debug("Verifying requesting user is authorized to perform this action");
            if (!upload.getUser().getId().equals(requestingUserId)) {
                throw new UnauthorizedUploadAccessException("You can only modify your own uploads");
            }

            logger.debug("Completing parsing process");
            upload.completeParsing();
            Upload completedUpload = uploadRepository.save(upload);
            logger.info("Successfully completed parsing process");
            return convertToDto(completedUpload);
        } catch (UploadException e) {
            logger.error("Parsing upload failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to complete parsing process: {}", e.getMessage());
            throw new UploadOperationException("Failed to complete parsing process", e);
        }
    }

    /**
     * Marks an upload as failed during parsing.
     * Only the upload owner can perform this operation.
     * Updates parsing status to FAILED, sets completion time, and stores error
     * message.
     * 
     * @param uploadId         the UUID of the upload to mark as failed
     * @param errorMessage     the error message describing why parsing failed
     * @param requestingUserId the UUID of the user making the request
     * @return the updated upload
     * @throws InvalidUploadParameterException   if any nonnull arg is null
     * @throws UploadNotFoundException           if the upload does not exist
     * @throws UnauthorizedUploadAccessException if the requesting user is not the
     *                                           upload owner
     * @throws UploadOperationException          if server error occurs
     * 
     */
    public UploadDto.GetResponse failParsingUpload(UUID uploadId, String errorMessage,
            UUID requestingUserId) {
        if (uploadId == null) {
            throw new InvalidUploadParameterException("uploadId cannot be null");
        }

        if (errorMessage == null) {
            throw new InvalidUploadParameterException("Error message cannot be null");
        }

        if (requestingUserId == null) {
            throw new InvalidUploadParameterException("Requesting userId cannot be null");
        }

        try {
            Upload upload = uploadRepository.findById(uploadId)
                    .orElseThrow(() -> new UploadNotFoundException("Upload not found"));

            logger.debug("Verifying requesting user is authorized to perform this action");
            if (!upload.getUser().getId().equals(requestingUserId)) {
                throw new UnauthorizedUploadAccessException("You can only modify your own uploads");
            }

            logger.debug("Assigning parsing process as failed with error message");
            upload.failParsing(errorMessage);
            Upload failedUpload = uploadRepository.save(upload);
            logger.info("Successfully assigned parsing process as failed with error message");
            return convertToDto(failedUpload);
        } catch (UploadException e) {
            logger.error("Failed to assign parsing status as failed with error message: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to assign parsing status as failed with error message: {}", e.getMessage());
            throw new UploadOperationException("Failed to assign parsing status as failed with error message", e);
        }
    }

    /**
     * Deletes an upload by its unique identifier.
     * Only the upload owner can perform this operation.
     * Maintains bidirectional relationships by removing upload from both user's and
     * deck's collections.
     * 
     * @param uploadId         the UUID of the upload to delete
     * @param requestingUserId the UUID of the user making the request
     * @throws InvalidUploadParameterException   if any nonnull arg is null
     * @throws UploadNotFoundException           if the upload does not exist
     * @throws UnauthorizedUploadAccessException if the requesting user is not the
     *                                           upload owner
     * @throws UploadOperationException          if server error occurs
     */
    public void deleteUploadById(UUID uploadId, UUID requestingUserId) {
        if (uploadId == null) {
            throw new InvalidUploadParameterException("uploadId cannot be null");
        }

        if (requestingUserId == null) {
            throw new InvalidUploadParameterException("Requesting userId cannot be null");
        }

        try {
            Upload upload = uploadRepository.findById(uploadId)
                    .orElseThrow(() -> new UploadNotFoundException("Upload not found"));

            logger.debug("Verifying requesting user is authorized to perform this action");
            if (!upload.getUser().getId().equals(requestingUserId)) {
                throw new UnauthorizedUploadAccessException("You can only delete your own uploads");
            }

            logger.debug("Grabbing parent deck and user");
            User user = upload.getUser();
            Deck deck = upload.getDeck();

            logger.debug("Deleting upload");
            uploadRepository.deleteById(uploadId);
            logger.info("Successfully deleted upload");

            user.removeUpload(upload);
            deck.removeUpload(upload);
            logger.info("Successfully removed deleted upload from parents");
        } catch (UploadException e) {
            logger.error("Upload delete failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to delete upload with provided id: {}", e.getMessage());
            throw new UploadOperationException("Failed to delete upload with provided id", e);
        }
    }

    /**
     * Deletes all uploads belonging to a specific user.
     * This is typically used for administrative purposes or account deletion.
     * Maintains bidirectional relationships by removing uploads from both user's
     * and decks' collections.
     * Only admin users can perform this operation.
     * 
     * @param userId the UUID of the user whose uploads to delete
     * @throws InvalidUploadParameterException   if any nonnull arg is null
     * @throws UnauthorizedUploadAccessException if requesting user is not an admin
     *                                           or not authenticated
     * @throws UserNotFoundException             if the specified user does not
     *                                           exist
     * @throws UploadOperationException          if server error occurs
     */
    public void deleteAllUserUploads(UUID userId) {
        if (userId == null) {
            throw new InvalidUploadParameterException("userId cannot be null");
        }

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null) {
                throw new UnauthorizedUploadAccessException("Authentication required");
            }

            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            logger.debug("Checking if user is admin");
            if (!isAdmin) {
                throw new UnauthorizedUploadAccessException("Admin access required");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            logger.debug("Fetching all uploads belonging to the provided user");
            List<Upload> uploads = uploadRepository.findByUserId(userId);
            logger.debug("Deleting all uploads belonging to the provided user");
            uploadRepository.deleteByUserId(userId);
            logger.info("Successfully deleted all uploads belonging to the provided user");

            logger.debug("Removing uploads from their corresponding parent deck");
            for (Upload upload : uploads) {
                Deck deck = upload.getDeck();
                deck.removeUpload(upload);
            }

            logger.debug("Clearing all uploads from the provided user");
            user.getUploads().clear();
            logger.info("Successfully removed uploads from their parent decks and user");
        } catch (UserException e) {
            logger.error("User's upload deletions failed due to user issue: {}", e.getMessage());
            throw e;
        } catch (UploadException e) {
            logger.error("Upload operation failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to delete all uploads belonging to provided user: {}", e.getMessage());
            throw new UploadOperationException("Failed to delete all uploads belonging to provided user", e);
        }
    }

    /**
     * Deletes all uploads belonging to a specific deck.
     * This is typically used when a deck is deleted or for cleanup purposes.
     * Maintains bidirectional relationships by removing uploads from both deck's
     * and users' collections.
     * Only admin users can perform this operation.
     * 
     * @param deckId the UUID of the deck whose uploads to delete
     * @throws InvalidUploadParameterException   if any nonnull arg is null
     * @throws UnauthorizedUploadAccessException if requesting user is not an admin
     *                                           or not authenticated
     * @throws DeckNotFoundException             if the specified deck does not
     *                                           exist
     * @throws UploadOperationException          if server error occurs
     */
    public void deleteAllDeckUploads(UUID deckId) {
        if (deckId == null) {
            throw new InvalidUploadParameterException("deckId cannot be null");
        }

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null) {
                throw new UnauthorizedUploadAccessException("Authentication required");
            }

            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            logger.debug("Checking if user is admin");
            if (!isAdmin) {
                throw new UnauthorizedUploadAccessException("Admin access required");
            }

            Deck deck = deckRepository.findById(deckId)
                    .orElseThrow(() -> new DeckNotFoundException("Deck with ID " + deckId + " not found"));

            logger.debug("Fetching all uploads belonging to the provided deck");
            List<Upload> uploads = uploadRepository.findByDeckId(deckId);
            logger.debug("Deleting all uploads belonging to the provided deck");
            uploadRepository.deleteByDeckId(deckId);
            logger.info("Successfully deleted all uploads belonging to the provided deck");

            logger.debug("Removing uploads from their corresponding parent user");
            for (Upload upload : uploads) {
                User user = upload.getUser();
                user.removeUpload(upload);
            }

            logger.debug("Clearing all uploads from the provided deck");
            deck.getUploads().clear();
            logger.info("Successfully removed uploads from their parent deck and user");
        } catch (DeckException e) {
            logger.error("Deck's upload deletions failed due to deck issue: {}", e.getMessage());
            throw e;
        } catch (UploadException e) {
            logger.error("Upload operation failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to delete all uploads belonging to provided deck: {}", e.getMessage());
            throw new UploadOperationException("Failed to delete all uploads belonging to provided deck", e);
        }
    }

    private UploadDto.GetResponse convertToDto(Upload upload) {
        return new UploadDto.GetResponse(upload.getId(), upload.getUser().getId(), upload.getDeck().getId(),
                upload.getFileName(), upload.getFileUrl(), upload.getFileType(), upload.getIsParsed(),
                upload.getParsingStatus(), upload.getErrorMessage(), upload.getParsingStartedAt(),
                upload.getParsingCompletedAt(), upload.getCreatedAt(), upload.getUpdatedAt());
    }
}
