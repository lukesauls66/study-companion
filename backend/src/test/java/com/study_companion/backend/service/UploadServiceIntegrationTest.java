package com.study_companion.backend.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.UUID;

import com.study_companion.backend.dto.DeckDto;
import com.study_companion.backend.dto.UploadDto;
import com.study_companion.backend.dto.UserDto;
import com.study_companion.backend.exception.deck.DeckNotFoundException;
import com.study_companion.backend.exception.upload.InvalidUploadCreationException;
import com.study_companion.backend.exception.upload.InvalidUploadParameterException;
import com.study_companion.backend.exception.upload.UnauthorizedUploadAccessException;
import com.study_companion.backend.exception.upload.UploadNotFoundException;
import com.study_companion.backend.exception.user.UserNotFoundException;
import com.study_companion.backend.model.FileType;
import com.study_companion.backend.model.postgres.Deck;
import com.study_companion.backend.model.postgres.Upload;
import com.study_companion.backend.model.postgres.User;
import com.study_companion.backend.repository.postgres.UserRepository;

@SpringBootTest
@Transactional
@Rollback
public class UploadServiceIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UploadService uploadService;

    @Autowired
    private UserService userService;

    @Autowired
    private DeckService deckService;

    @Test
    void createUpload_ValidInput_ReturnsUpload() {
        UserDto.Create userCreateDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");
        UserDto.Get user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.id(), "Test Deck", "Testing");
        Deck deck = deckService.createDeck(deckCreateDto);

        UploadDto.Create uploadCreateDto = new UploadDto.Create(user.id(), deck.getId(), "test-file.png",
                FileType.PNG, "https://example.com/files/test-file.png"); 
        Long fileSize = 1024L; 

        Upload upload = uploadService.createUpload(uploadCreateDto, fileSize);

        // Verify upload properties
        assertThat(upload).isNotNull(); 
        assertThat(upload.getId()).isNotNull();
        assertThat(upload.getUser().getId()).isEqualTo(user.id());
        assertThat(upload.getDeck().getId()).isEqualTo(deck.getId());
        assertThat(upload.getFileName()).isEqualTo("test-file.png");
        assertThat(upload.getFileUrl()).isEqualTo("https://example.com/files/test-file.png");
        assertThat(upload.getFileType()).isEqualTo(FileType.PNG);
        assertThat(upload.getFileSize()).isEqualTo(1024L);
        assertThat(upload.getCreatedAt()).isNotNull();
        assertThat(upload.getUpdatedAt()).isNotNull();

        // Verify bidirectional relationship is maintained
        User refreshedUser = userRepository.findById(user.id())
        .orElseThrow(() -> new UserNotFoundException("User not found"));
        assertThat(refreshedUser.getUploads()).hasSize(1);
        assertThat(refreshedUser.getUploads()).extracting(Upload::getFileName).contains("test-file.png");

        Deck refreshedDeck = deckService.getDeckById(deck.getId());
        assertThat(refreshedDeck.getUploads()).hasSize(1);
        assertThat(refreshedDeck.getUploads()).extracting(Upload::getFileName).contains("test-file.png");
    }

    @Test
    void createUpload_InvalidInput_ThrowsInvalidUploadCreationException() {
        UserDto.Create userCreateDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");
        UserDto.Get user = userService.createUser(userCreateDto);
 
        DeckDto.Create deckCreateDto = new DeckDto.Create(user.id(), "Test Deck", "Testing");
        Deck deck = deckService.createDeck(deckCreateDto);

        UploadDto.Create uploadCreateDto = new UploadDto.Create(user.id(), deck.getId(), "", FileType.PNG, "https://example.com/files/test-file.PDF");
        Long fileSize = 1024L;

        InvalidUploadCreationException exception = assertThrows(InvalidUploadCreationException.class, () -> {
            uploadService.createUpload(uploadCreateDto, fileSize);
        });

        assertThat(exception.getMessage()).isEqualTo("File name cannot be empty");
    }

    @Test
    void createUpload_NullUploadDto_ThrowsInvalidUploadParameterException() {
        InvalidUploadParameterException exception = assertThrows(InvalidUploadParameterException.class, () -> {
            uploadService.createUpload(null, 1024L);
        });

        assertThat(exception.getMessage()).isEqualTo("Upload data transfer object cannot be null");
    }

    @Test
    void createUpload_NullFileSize_ThrowsInvalidUploadParameterException() {
        UUID userId = UUID.randomUUID();
        UUID deckId = UUID.randomUUID(); 
        UploadDto.Create uploadCreateDto = new UploadDto.Create(userId, deckId, "test-file.PDF", FileType.PNG, "https://example.com/file.PDF");

        InvalidUploadParameterException exception = assertThrows(InvalidUploadParameterException.class, () -> {
            uploadService.createUpload(uploadCreateDto, null);
        });

        assertThat(exception.getMessage()).isEqualTo("File size cannot be null");
    }

    @Test
    void createUpload_NonExistentUser_ThrowsUserNotFoundException() {
        UserDto.Create userCreateDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");
        UserDto.Get user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.id(), "Test Deck", "Testing");
        Deck deck = deckService.createDeck(deckCreateDto);

        UUID nonExistentUserId = UUID.randomUUID();
        UploadDto.Create uploadCreateDto = new UploadDto.Create(nonExistentUserId, deck.getId(), "test-file.PDF",
                FileType.PNG, "https://example.com/file.PDF"); 

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            uploadService.createUpload(uploadCreateDto, 1024L);
        });

        assertThat(exception.getMessage()).contains("User not found");
    }

    @Test
    void createUpload_NonExistentDeck_ThrowsDeckNotFoundException() {
        UserDto.Create userCreateDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");
        UserDto.Get user = userService.createUser(userCreateDto);

        UUID nonExistentDeckId = UUID.randomUUID();
        UploadDto.Create uploadCreateDto = new UploadDto.Create(user.id(), nonExistentDeckId, "test-file.PDF",
                FileType.PNG, "https://example.com/file.PDF");

        DeckNotFoundException exception = assertThrows(DeckNotFoundException.class, () -> {
            uploadService.createUpload(uploadCreateDto, 1024L);
        });

        assertThat(exception.getMessage()).contains("Deck with ID " + nonExistentDeckId + " not found");
    }

    @Test
    void getUploadById_ValidInput_ReturnsUpload() {
        UserDto.Create userCreateDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");
        UserDto.Get user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.id(), "Test Deck", "Testing");
        Deck deck = deckService.createDeck(deckCreateDto);

        UploadDto.Create uploadCreateDto = new UploadDto.Create(user.id(), deck.getId(), "test-file.PDF",
                FileType.PNG, "https://example.com/file.PDF");
        Upload upload = uploadService.createUpload(uploadCreateDto, 1024L);
 
        Upload retrievedUpload = uploadService.getUploadById(upload.getId());

        assertThat(retrievedUpload).isNotNull();
        assertThat(retrievedUpload.getId()).isEqualTo(upload.getId());
        assertThat(retrievedUpload.getFileName()).isEqualTo("test-file.PDF");
        assertThat(retrievedUpload.getFileType()).isEqualTo(FileType.PNG);
    }

    @Test
    void getUploadById_NullId_ThrowsInvalidUploadParameterException() {
        InvalidUploadParameterException exception = assertThrows(InvalidUploadParameterException.class, () -> {
            uploadService.getUploadById(null);
        });

        assertThat(exception.getMessage()).isEqualTo("id cannot be null");
    }

    @Test
    void getUploadById_NonExistentId_ThrowsUploadNotFoundException() {
        UUID nonExistentId = UUID.randomUUID();

        UploadNotFoundException exception = assertThrows(UploadNotFoundException.class, () -> {
            uploadService.getUploadById(nonExistentId);
        });

        assertThat(exception.getMessage()).isEqualTo("Upload with ID " + nonExistentId + " not found");
    }

    @Test
    void getAllUserUploads_NullUserId_ThrowsInvalidUploadParameterException() {
        InvalidUploadParameterException exception = assertThrows(InvalidUploadParameterException.class, () -> {
            uploadService.getAllUserUploads(null);
        });

        assertThat(exception.getMessage()).isEqualTo("userId cannot be null");
    }

    @Test
    void getAllUserUploads_ValidInput_ReturnsUploads() {
        UserDto.Create userCreateDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");
        UserDto.Get user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.id(), "Test Deck", "Testing");
        Deck deck = deckService.createDeck(deckCreateDto);

        UploadDto.Create uploadCreateDto1 = new UploadDto.Create(user.id(), deck.getId(), "file1.PDF", FileType.PNG, "https://example.com/file1.PDF");
        UploadDto.Create uploadCreateDto2 = new UploadDto.Create(user.id(), deck.getId(), "file2.JPG", FileType.JPG, "https://example.com/file2.JPG");

        uploadService.createUpload(uploadCreateDto1, 1024L);
        uploadService.createUpload(uploadCreateDto2, 2048L);

        List<Upload> uploads = uploadService.getAllUserUploads(user.id());

        assertThat(uploads).hasSize(2);
        assertThat(uploads).extracting(Upload::getFileName).containsExactlyInAnyOrder("file1.PDF", "file2.JPG");
    }

    @Test
    void getAllDeckUploads_NullDeckId_ThrowsInvalidUploadParameterException() {
        InvalidUploadParameterException exception = assertThrows(InvalidUploadParameterException.class, () -> {
            uploadService.getAllDeckUploads(null);
        });

        assertThat(exception.getMessage()).isEqualTo("deckId cannot be null");
    }

    @Test
    void getAllDeckUploads_ValidInput_ReturnsUploads() {
        UserDto.Create userCreateDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");
        UserDto.Get user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.id(), "Test Deck", "Testing");
        Deck deck = deckService.createDeck(deckCreateDto);

        UploadDto.Create uploadCreateDto1 = new UploadDto.Create(user.id(), deck.getId(), "file1.PDF", FileType.PNG, "https://example.com/file1.PDF");
        UploadDto.Create uploadCreateDto2 = new UploadDto.Create(user.id(), deck.getId(), "file2.JPG", FileType.JPG, "https://example.com/file2.JPG");

        uploadService.createUpload(uploadCreateDto1, 1024L);
        uploadService.createUpload(uploadCreateDto2, 2048L); 

        List<Upload> uploads = uploadService.getAllDeckUploads(deck.getId());

        assertThat(uploads).hasSize(2);
        assertThat(uploads).extracting(Upload::getFileName).containsExactlyInAnyOrder("file1.PDF", "file2.JPG");
    }

    @Test
    void getCountOfAllUserUploads_NullUserId_ThrowsInvalidUploadParameterException() {
        InvalidUploadParameterException exception = assertThrows(InvalidUploadParameterException.class, () -> {
            uploadService.getCountOfAllUserUploads(null);
        });

        assertThat(exception.getMessage()).isEqualTo("userId cannot be null");
    }

    @Test
    void getCountOfAllUserUploads_ValidInput_ReturnsCount() {
        UserDto.Create userCreateDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");
        UserDto.Get user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.id(), "Test Deck", "Testing");
        Deck deck = deckService.createDeck(deckCreateDto);

        UploadDto.Create uploadCreateDto1 = new UploadDto.Create(user.id(), deck.getId(), "file1.PDF", FileType.PNG, "https://example.com/file1.PDF");
        UploadDto.Create uploadCreateDto2 = new UploadDto.Create(user.id(), deck.getId(), "file2.JPG", FileType.JPG, "https://example.com/file2.JPG");

        uploadService.createUpload(uploadCreateDto1, 1024L);
        uploadService.createUpload(uploadCreateDto2, 2048L);

        long count = uploadService.getCountOfAllUserUploads(user.id());

        assertThat(count).isEqualTo(2);
    }

    @Test
    void getCountOfAllDeckUploads_NullDeckId_ThrowsInvalidUploadParameterException() {
        InvalidUploadParameterException exception = assertThrows(InvalidUploadParameterException.class, () -> {
            uploadService.getCountOfAllDeckUploads(null);
        });

        assertThat(exception.getMessage()).isEqualTo("deckId cannot be null");
    }

    @Test
    void getCountOfAllDeckUploads_ValidInput_ReturnsCount() {
        UserDto.Create userCreateDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");
        UserDto.Get user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.id(), "Test Deck", "Testing");
        Deck deck = deckService.createDeck(deckCreateDto);

        UploadDto.Create uploadCreateDto1 = new UploadDto.Create(user.id(), deck.getId(), "file1.PDF", FileType.PNG, "https://example.com/file1.PDF");
        UploadDto.Create uploadCreateDto2 = new UploadDto.Create(user.id(), deck.getId(), "file2.JPG", FileType.JPG, "https://example.com/file2.JPG");

        uploadService.createUpload(uploadCreateDto1, 1024L);
        uploadService.createUpload(uploadCreateDto2, 2048L);

        long count = uploadService.getCountOfAllDeckUploads(deck.getId());

        assertThat(count).isEqualTo(2);
    }

    @Test
    void startParsingUpload_NullUploadId_ThrowsInvalidUploadParameterException() {
        UUID userId = UUID.randomUUID();

        InvalidUploadParameterException exception = assertThrows(InvalidUploadParameterException.class, () -> {
            uploadService.startParsingUpload(null, userId);
        });

        assertThat(exception.getMessage()).isEqualTo("uploadId cannot be null");
    }

    @Test
    void startParsingUpload_NullRequestingUserId_ThrowsInvalidUploadParameterException() {
        UserDto.Create userCreateDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");
        UserDto.Get user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.id(), "Test Deck", "Testing");
        Deck deck = deckService.createDeck(deckCreateDto);

        UploadDto.Create uploadCreateDto = new UploadDto.Create(user.id(), deck.getId(), "test-file.PDF",
                FileType.PNG, "https://example.com/file.PDF");
        Upload upload = uploadService.createUpload(uploadCreateDto, 1024L);

        InvalidUploadParameterException exception = assertThrows(InvalidUploadParameterException.class, () -> {
            uploadService.startParsingUpload(upload.getId(), null);
        });
 
        assertThat(exception.getMessage()).isEqualTo("Requesting userId cannot be null");
    }

    @Test
    void startParsingUpload_UnauthorizedUser_ThrowsUnauthorizedUploadAccessException() {
        UserDto.Create userCreateDto1 = new UserDto.Create("test1@email.com", "John Smith", "john123", "password");
        UserDto.Get user1 = userService.createUser(userCreateDto1);

        UserDto.Create userCreateDto2 = new UserDto.Create("test2@email.com", "Jane Doe", "jane123", "password");
        UserDto.Get user2 = userService.createUser(userCreateDto2);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user1.id(), "Test Deck", "Testing");
        Deck deck = deckService.createDeck(deckCreateDto);

        UploadDto.Create uploadCreateDto = new UploadDto.Create(user1.id(), deck.getId(), "test-file.PDF",
                FileType.PNG, "https://example.com/file.PDF");
        Upload upload = uploadService.createUpload(uploadCreateDto, 1024L);
 
        UnauthorizedUploadAccessException exception = assertThrows(UnauthorizedUploadAccessException.class, () -> {
            uploadService.startParsingUpload(upload.getId(), user2.id());
        });

        assertThat(exception.getMessage()).isEqualTo("You can only modify your own uploads");
    }

    @Test
    void completeParsingUpload_NullUploadId_ThrowsInvalidUploadParameterException() {
        UUID userId = UUID.randomUUID();

        InvalidUploadParameterException exception = assertThrows(InvalidUploadParameterException.class, () -> {
            uploadService.completeParsingUpload(null, userId);
        });

        assertThat(exception.getMessage()).isEqualTo("uploadId cannot be null");
    }

    @Test
    void completeParsingUpload_NullRequestingUserId_ThrowsInvalidUploadParameterException() {
        UserDto.Create userCreateDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");
        UserDto.Get user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.id(), "Test Deck", "Testing");
        Deck deck = deckService.createDeck(deckCreateDto);

        UploadDto.Create uploadCreateDto = new UploadDto.Create(user.id(), deck.getId(), "test-file.PDF",
                FileType.PNG, "https://example.com/file.PDF");  
        Upload upload = uploadService.createUpload(uploadCreateDto, 1024L);

        InvalidUploadParameterException exception = assertThrows(InvalidUploadParameterException.class, () -> {
            uploadService.completeParsingUpload(upload.getId(), null);
        });

        assertThat(exception.getMessage()).isEqualTo("Requesting userId cannot be null");
    }

    @Test
    void failParsingUpload_NullUploadId_ThrowsInvalidUploadParameterException() {
        UUID userId = UUID.randomUUID();

        InvalidUploadParameterException exception = assertThrows(InvalidUploadParameterException.class, () -> {
            uploadService.failParsingUpload(null, "Error message", userId);
        });

        assertThat(exception.getMessage()).isEqualTo("uploadId cannot be null");
    }

    @Test
    void failParsingUpload_NullErrorMessage_ThrowsInvalidUploadParameterException() {
        UserDto.Create userCreateDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");
        UserDto.Get user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.id(), "Test Deck", "Testing");
        Deck deck = deckService.createDeck(deckCreateDto);

        UploadDto.Create uploadCreateDto = new UploadDto.Create(user.id(), deck.getId(), "test-file.PDF",
                FileType.PNG, "https://example.com/file.PDF");
        Upload upload = uploadService.createUpload(uploadCreateDto, 1024L);

        InvalidUploadParameterException exception = assertThrows(InvalidUploadParameterException.class, () -> {
            uploadService.failParsingUpload(upload.getId(), null, user.id());
        });

        assertThat(exception.getMessage()).isEqualTo("Error message cannot be null");
    }

    @Test
    void failParsingUpload_NullRequestingUserId_ThrowsInvalidUploadParameterException() {
        UserDto.Create userCreateDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");
        UserDto.Get user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.id(), "Test Deck", "Testing");
        Deck deck = deckService.createDeck(deckCreateDto);

        UploadDto.Create uploadCreateDto = new UploadDto.Create(user.id(), deck.getId(), "test-file.PDF",
                FileType.PNG, "https://example.com/file.PDF");
        Upload upload = uploadService.createUpload(uploadCreateDto, 1024L);

        InvalidUploadParameterException exception = assertThrows(InvalidUploadParameterException.class, () -> {
            uploadService.failParsingUpload(upload.getId(), "Error message", null);
        });

        assertThat(exception.getMessage()).isEqualTo("Requesting userId cannot be null");
    }

    @Test
    void deleteUploadById_NullUploadId_ThrowsInvalidUploadParameterException() {
        UUID userId = UUID.randomUUID();

        InvalidUploadParameterException exception = assertThrows(InvalidUploadParameterException.class, () -> {
            uploadService.deleteUploadById(null, userId);
        });

        assertThat(exception.getMessage()).isEqualTo("uploadId cannot be null");
    }

    @Test
    void deleteUploadById_NullRequestingUserId_ThrowsInvalidUploadParameterException() {
        UserDto.Create userCreateDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");
        UserDto.Get user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.id(), "Test Deck", "Testing");
        Deck deck = deckService.createDeck(deckCreateDto);

        UploadDto.Create uploadCreateDto = new UploadDto.Create(user.id(), deck.getId(), "test-file.PDF",
                FileType.PNG, "https://example.com/file.PDF");
        Upload upload = uploadService.createUpload(uploadCreateDto, 1024L);

        InvalidUploadParameterException exception = assertThrows(InvalidUploadParameterException.class, () -> {
            uploadService.deleteUploadById(upload.getId(), null);
        });

        assertThat(exception.getMessage()).isEqualTo("Requesting userId cannot be null");
    }

    @Test
    void deleteUploadById_UnauthorizedUser_ThrowsUnauthorizedUploadAccessException() {
        UserDto.Create userCreateDto1 = new UserDto.Create("test1@email.com", "John Smith", "john123", "password");
        UserDto.Get user1 = userService.createUser(userCreateDto1);

        UserDto.Create userCreateDto2 = new UserDto.Create("test2@email.com", "Jane Doe", "jane123", "password");
        UserDto.Get user2 = userService.createUser(userCreateDto2);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user1.id(), "Test Deck", "Testing");
        Deck deck = deckService.createDeck(deckCreateDto);

        UploadDto.Create uploadCreateDto = new UploadDto.Create(user1.id(), deck.getId(), "test-file.PDF",
                FileType.PNG, "https://example.com/file.PDF");
        Upload upload = uploadService.createUpload(uploadCreateDto, 1024L);

        UnauthorizedUploadAccessException exception = assertThrows(UnauthorizedUploadAccessException.class, () -> {
            uploadService.deleteUploadById(upload.getId(), user2.id());
        });

        assertThat(exception.getMessage()).isEqualTo("You can only delete your own uploads");
    }

    @Test 
    void deleteUploadById_ValidInput_DeletesUpload() {
        UserDto.Create userCreateDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");
        UserDto.Get user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.id(), "Test Deck", "Testing");
        Deck deck = deckService.createDeck(deckCreateDto);
 
        UploadDto.Create uploadCreateDto1 = new UploadDto.Create(user.id(), deck.getId(), "file1.PDF", FileType.PNG, "https://example.com/file1.PDF");
        UploadDto.Create uploadCreateDto2 = new UploadDto.Create(user.id(), deck.getId(), "file2.JPG", FileType.JPG, "https://example.com/file2.JPG");

        Upload upload1 = uploadService.createUpload(uploadCreateDto1, 1024L);
        Upload upload2 = uploadService.createUpload(uploadCreateDto2, 2048L);

        // Verify uploads exist
        assertThat(uploadService.getAllUserUploads(user.id())).hasSize(2);

        uploadService.deleteUploadById(upload1.getId(), user.id());

        // Verify upload deleted
        List<Upload> remainingUploads = uploadService.getAllUserUploads(user.id());
        assertThat(remainingUploads).hasSize(1);
        assertThat(remainingUploads.get(0).getId()).isEqualTo(upload2.getId());

        // Verify bidirectional relationship maintained
        User refreshedUser = userRepository.findById(user.id())
        .orElseThrow(() -> new UserNotFoundException("User not found"));
        assertThat(refreshedUser.getUploads()).hasSize(1);
 
        Deck refreshedDeck = deckService.getDeckById(deck.getId());
        assertThat(refreshedDeck.getUploads()).hasSize(1);
    }

    @Test
    void deleteAllUserUploads_NullUserId_ThrowsInvalidUploadParameterException() {
        InvalidUploadParameterException exception = assertThrows(InvalidUploadParameterException.class, () -> {
            uploadService.deleteAllUserUploads(null);
        });

        assertThat(exception.getMessage()).isEqualTo("userId cannot be null");
    }

    @Test
    void deleteAllUserUploads_NonExistentUser_ThrowsUserNotFoundException() {
        UUID nonExistentUserId = UUID.randomUUID();

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            uploadService.deleteAllUserUploads(nonExistentUserId);
        });

        assertThat(exception.getMessage()).contains("User not found");
    }

    @Test
    void deleteAllUserUploads_ValidInput_DeletesAllUploads() {
        UserDto.Create userCreateDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");
        UserDto.Get user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.id(), "Test Deck", "Testing");
        Deck deck = deckService.createDeck(deckCreateDto);

        UploadDto.Create uploadCreateDto1 = new UploadDto.Create(user.id(), deck.getId(), "file1.PDF", FileType.PNG, "https://example.com/file1.PDF");
        UploadDto.Create uploadCreateDto2 = new UploadDto.Create(user.id(), deck.getId(), "file2.JPG", FileType.JPG, "https://example.com/file2.JPG");

        uploadService.createUpload(uploadCreateDto1, 1024L);
        uploadService.createUpload(uploadCreateDto2, 2048L);

        // Verify uploads exist
        assertThat(uploadService.getAllUserUploads(user.id())).hasSize(2);

        uploadService.deleteAllUserUploads(user.id());

        // Verify all uploads deleted
        assertThat(uploadService.getAllUserUploads(user.id())).hasSize(0);

        // Verify bidirectional relationship maintained
        User refreshedUser = userRepository.findById(user.id())
        .orElseThrow(() -> new UserNotFoundException("User not found"));
        assertThat(refreshedUser.getUploads()).hasSize(0);

        Deck refreshedDeck = deckService.getDeckById(deck.getId()); 
        assertThat(refreshedDeck.getUploads()).hasSize(0);
    }

    @Test
    void deleteAllDeckUploads_NullDeckId_ThrowsInvalidUploadParameterException() {
        InvalidUploadParameterException exception = assertThrows(InvalidUploadParameterException.class, () -> {
            uploadService.deleteAllDeckUploads(null);
        });

        assertThat(exception.getMessage()).isEqualTo("deckId cannot be null");
    }

    @Test
    void deleteAllDeckUploads_NonExistentDeck_ThrowsDeckNotFoundException() {
        UUID nonExistentDeckId = UUID.randomUUID();

        DeckNotFoundException exception = assertThrows(DeckNotFoundException.class, () -> {
            uploadService.deleteAllDeckUploads(nonExistentDeckId);
        });

        assertThat(exception.getMessage()).contains("Deck with ID " + nonExistentDeckId + " not found");
    }

    @Test
    void deleteAllDeckUploads_ValidInput_DeletesAllUploads() {
        UserDto.Create userCreateDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");
        UserDto.Get user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.id(), "Test Deck", "Testing");
        Deck deck = deckService.createDeck(deckCreateDto);

        UploadDto.Create uploadCreateDto1 = new UploadDto.Create(user.id(), deck.getId(), "file1.PDF", FileType.PNG, "https://example.com/file1.PDF");
        UploadDto.Create uploadCreateDto2 = new UploadDto.Create(user.id(), deck.getId(), "file2.JPG", FileType.JPG, "https://example.com/file2.JPG");

        uploadService.createUpload(uploadCreateDto1, 1024L);
        uploadService.createUpload(uploadCreateDto2, 2048L);

        // Verify uploads exist
        assertThat(uploadService.getAllDeckUploads(deck.getId())).hasSize(2);

        uploadService.deleteAllDeckUploads(deck.getId());

        // Verify all uploads deleted
        assertThat(uploadService.getAllDeckUploads(deck.getId())).hasSize(0);
 
        // Verify bidirectional relationship maintained
        User refreshedUser = userRepository.findById(user.id())
        .orElseThrow(() -> new UserNotFoundException("User not found"));
        assertThat(refreshedUser.getUploads()).hasSize(0);

        Deck refreshedDeck = deckService.getDeckById(deck.getId());
        assertThat(refreshedDeck.getUploads()).hasSize(0);
    }
}
