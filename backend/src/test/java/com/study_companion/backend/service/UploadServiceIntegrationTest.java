package com.study_companion.backend.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
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
import com.study_companion.backend.exception.deck.UnauthorizedDeckAccessException;
import com.study_companion.backend.exception.upload.InvalidUploadCreationException;
import com.study_companion.backend.exception.upload.InvalidUploadParameterException;
import com.study_companion.backend.exception.upload.UnauthorizedUploadAccessException;
import com.study_companion.backend.exception.upload.UploadException;
import com.study_companion.backend.exception.upload.UploadNotFoundException;
import com.study_companion.backend.exception.user.UserNotFoundException;
import com.study_companion.backend.model.FileType;
import com.study_companion.backend.model.ParsingStatus;
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
        @WithMockUser(roles = "ADMIN")
        void createUpload_ValidInput_ReturnsUpload() {
                UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith",
                                "john123",
                                "password");
                UserDto.GetResponse user = userService.createUser(userCreateDto);

                DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");
                DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

                UploadDto.CreateRequest uploadCreateDto = new UploadDto.CreateRequest(user.id(), deck.deckId(),
                                "test-file.png",
                                FileType.PNG, "https://example.com/files/test-file.png");
                Long fileSize = 1024L;

                UploadDto.GetResponse upload = uploadService.createUpload(uploadCreateDto, fileSize, user.id());

                // Verify upload properties
                assertThat(upload).isNotNull();
                assertThat(upload.id()).isNotNull();
                assertThat(upload.userId()).isEqualTo(user.id());
                assertThat(upload.deckId()).isEqualTo(deck.deckId());
                assertThat(upload.fileName()).isEqualTo("test-file.png");
                assertThat(upload.fileUrl()).isEqualTo("https://example.com/files/test-file.png");
                assertThat(upload.fileType()).isEqualTo(FileType.PNG);
                assertThat(upload.createdAt()).isNotNull();
                assertThat(upload.updatedAt()).isNotNull();

                // Verify bidirectional relationship is maintained
                User refreshedUser = userRepository.findById(user.id())
                                .orElseThrow(() -> new UserNotFoundException("User not found"));
                assertThat(refreshedUser.getUploads()).hasSize(1);
                assertThat(refreshedUser.getUploads()).extracting(Upload::getFileName).contains("test-file.png");

                DeckDto.GetResponseWithCardsAndUploads refreshedDeck = deckService
                                .getDeckByIdWithCardsAndUploads(deck.deckId());
                assertThat(refreshedDeck.uploads()).hasSize(1);
                assertThat(refreshedDeck.uploads()).extracting(UploadDto.GetResponse::fileName)
                                .contains("test-file.png");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void createUpload_InvalidInput_ThrowsInvalidUploadCreationException() {
                UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith",
                                "john123",
                                "password");
                UserDto.GetResponse user = userService.createUser(userCreateDto);

                DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");
                DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

                UploadDto.CreateRequest uploadCreateDto = new UploadDto.CreateRequest(user.id(), deck.deckId(), "",
                                FileType.PNG,
                                "https://example.com/files/test-file.PDF");
                Long fileSize = 1024L;

                InvalidUploadCreationException exception = assertThrows(InvalidUploadCreationException.class, () -> {
                        uploadService.createUpload(uploadCreateDto, fileSize, user.id());
                });

                assertThat(exception.getMessage()).isEqualTo("File name cannot be empty");
        }

        @Test
        void createUpload_NullUploadDto_ThrowsInvalidUploadParameterException() {
                UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith",
                                "john123",
                                "password");
                UserDto.GetResponse user = userService.createUser(userCreateDto);
                Long fileSize = 1024L;

                InvalidUploadParameterException exception = assertThrows(InvalidUploadParameterException.class, () -> {
                        uploadService.createUpload(null, fileSize, user.id());
                });

                assertThat(exception.getMessage()).isEqualTo("Upload data transfer object cannot be null");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void createUpload_NullRequestingUserId_ThrowsInvalidUploadParameterException() {
                UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith",
                                "john123",
                                "password");
                UserDto.GetResponse user = userService.createUser(userCreateDto);

                DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");
                DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

                UploadDto.CreateRequest uploadCreateDto = new UploadDto.CreateRequest(user.id(), deck.deckId(), "",
                                FileType.PNG,
                                "https://example.com/files/test-file.PDF");
                Long fileSize = 1024L;

                InvalidUploadParameterException exception = assertThrows(InvalidUploadParameterException.class, () -> {
                        uploadService.createUpload(uploadCreateDto, fileSize, null);
                });

                assertThat(exception.getMessage()).isEqualTo("Requesting userId cannot be null");
        }

        @Test
        void createUpload_NullFileSize_ThrowsInvalidUploadParameterException() {
                UUID userId = UUID.randomUUID();
                UUID deckId = UUID.randomUUID();
                UploadDto.CreateRequest uploadCreateDto = new UploadDto.CreateRequest(userId, deckId, "test-file.PDF",
                                FileType.PNG,
                                "https://example.com/file.PDF");

                InvalidUploadParameterException exception = assertThrows(InvalidUploadParameterException.class, () -> {
                        uploadService.createUpload(uploadCreateDto, null, userId);
                });

                assertThat(exception.getMessage()).isEqualTo("File size cannot be null");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void createUpload_NonExistentUser_ThrowsUserNotFoundException() {
                UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith",
                                "john123",
                                "password");
                UserDto.GetResponse user = userService.createUser(userCreateDto);

                DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");
                DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

                UUID nonExistentUserId = UUID.randomUUID();
                UploadDto.CreateRequest uploadCreateDto = new UploadDto.CreateRequest(nonExistentUserId, deck.deckId(),
                                "test-file.PDF",
                                FileType.PNG, "https://example.com/file.PDF");

                UploadException exception = assertThrows(UploadException.class, () -> {
                        uploadService.createUpload(uploadCreateDto, 1024L, user.id());
                });

                assertThat(exception.getMessage()).contains("Unauthorized user upload");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void createUpload_NonExistentDeck_ThrowsDeckNotFoundException() {
                UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith",
                                "john123",
                                "password");
                UserDto.GetResponse user = userService.createUser(userCreateDto);

                UUID nonExistentDeckId = UUID.randomUUID();
                UploadDto.CreateRequest uploadCreateDto = new UploadDto.CreateRequest(user.id(), nonExistentDeckId,
                                "test-file.PDF",
                                FileType.PNG, "https://example.com/file.PDF");

                DeckNotFoundException exception = assertThrows(DeckNotFoundException.class, () -> {
                        uploadService.createUpload(uploadCreateDto, 1024L, user.id());
                });

                assertThat(exception.getMessage()).contains("Deck with ID " + nonExistentDeckId + " not found");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void createUpload_InvalidUser_ThrowsUnauthorizedDeckAccessException() {
                UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith",
                                "john123",
                                "password");
                UserDto.GetResponse user = userService.createUser(userCreateDto);

                UserDto.CreateRequest userCreateDto2 = new UserDto.CreateRequest("test2@email.com", "Jane Smith",
                                "jane123",
                                "password2");
                UserDto.GetResponse user2 = userService.createUser(userCreateDto2);

                DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user2.id(), "Test Deck", "Testing");
                DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

                UploadDto.CreateRequest uploadCreateDto = new UploadDto.CreateRequest(user.id(), deck.deckId(),
                                "test-file.png",
                                FileType.PNG, "https://example.com/files/test-file.png");
                Long fileSize = 1024L;

                UnauthorizedDeckAccessException exception = assertThrows(UnauthorizedDeckAccessException.class, () -> {
                        uploadService.createUpload(uploadCreateDto, fileSize, user.id());
                });

                assertThat(exception.getMessage()).contains("You can only upload files to your own decks");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void createUpload_InvalidRequestingUser_ThrowsUnauthorizedUploadAccessException() {
                UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith",
                                "john123",
                                "password");
                UserDto.GetResponse user = userService.createUser(userCreateDto);

                DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");
                DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

                UploadDto.CreateRequest uploadCreateDto = new UploadDto.CreateRequest(user.id(), deck.deckId(),
                                "test-file.png",
                                FileType.PNG, "https://example.com/files/test-file.png");
                Long fileSize = 1024L;

                UUID invalidUserId = UUID.randomUUID();
                UnauthorizedUploadAccessException exception = assertThrows(UnauthorizedUploadAccessException.class,
                                () -> {
                                        uploadService.createUpload(uploadCreateDto, fileSize, invalidUserId);
                                });

                assertThat(exception.getMessage()).contains("Unauthorized user upload");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void createUpload_EmptyFileName_ThrowsInvalidUploadCreationException() {
                UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith",
                                "john123",
                                "password");
                UserDto.GetResponse user = userService.createUser(userCreateDto);

                DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");
                DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

                UploadDto.CreateRequest uploadCreateDto = new UploadDto.CreateRequest(user.id(), deck.deckId(), "",
                                FileType.PNG, "https://example.com/file.PDF");
                Long fileSize = 1024L;

                InvalidUploadCreationException exception = assertThrows(InvalidUploadCreationException.class, () -> {
                        uploadService.createUpload(uploadCreateDto, fileSize, user.id());
                });

                assertThat(exception.getMessage()).isEqualTo("File name cannot be empty");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void createUpload_EmptyFileUrl_ThrowsInvalidUploadCreationException() {
                UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith",
                                "john123",
                                "password");
                UserDto.GetResponse user = userService.createUser(userCreateDto);

                DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");
                DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

                UploadDto.CreateRequest uploadCreateDto = new UploadDto.CreateRequest(user.id(), deck.deckId(),
                                "test-file.PDF",
                                FileType.PNG, "");
                Long fileSize = 1024L;

                InvalidUploadCreationException exception = assertThrows(InvalidUploadCreationException.class, () -> {
                        uploadService.createUpload(uploadCreateDto, fileSize, user.id());
                });

                assertThat(exception.getMessage()).isEqualTo("File URL cannot be empty");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void createUpload_EmptyFileSize_ThrowsInvalidUploadCreationException() {
                UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith",
                                "john123",
                                "password");
                UserDto.GetResponse user = userService.createUser(userCreateDto);

                DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");
                DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

                UploadDto.CreateRequest uploadCreateDto = new UploadDto.CreateRequest(user.id(), deck.deckId(),
                                "test-file.PDF",
                                FileType.PNG, "https://example.com/file.PDF");

                InvalidUploadCreationException exception = assertThrows(InvalidUploadCreationException.class, () -> {
                        uploadService.createUpload(uploadCreateDto, 0L, user.id());
                });

                assertThat(exception.getMessage()).isEqualTo("File size must be greater than 0");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void getUploadById_ValidInput_ReturnsUpload() {
                UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith",
                                "john123",
                                "password");
                UserDto.GetResponse user = userService.createUser(userCreateDto);

                DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");
                DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

                UploadDto.CreateRequest uploadCreateDto = new UploadDto.CreateRequest(user.id(), deck.deckId(),
                                "test-file.PDF",
                                FileType.PNG, "https://example.com/file.PDF");
                UploadDto.GetResponse upload = uploadService.createUpload(uploadCreateDto, 1024L, user.id());

                UploadDto.GetResponse retrievedUpload = uploadService.getUploadById(upload.id());

                assertThat(retrievedUpload).isNotNull();
                assertThat(retrievedUpload.id()).isEqualTo(upload.id());
                assertThat(retrievedUpload.fileName()).isEqualTo("test-file.PDF");
                assertThat(retrievedUpload.fileType()).isEqualTo(FileType.PNG);
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
        @WithMockUser(roles = "ADMIN")
        void getAllUploads_ValidInput_ReturnsUploads() {
                UserDto.CreateRequest userCreateDto1 = new UserDto.CreateRequest("test1@email.com", "John Smith",
                                "john123",
                                "password");
                UserDto.GetResponse user1 = userService.createUser(userCreateDto1);

                UserDto.CreateRequest userCreateDto2 = new UserDto.CreateRequest("test2@email.com", "Jane Doe",
                                "jane123",
                                "password2");
                UserDto.GetResponse user2 = userService.createUser(userCreateDto2);

                DeckDto.CreateRequest deckCreateDto1 = new DeckDto.CreateRequest(user1.id(), "User1 Deck", "Testing");
                DeckDto.GetResponse deck1 = deckService.createDeck(deckCreateDto1);

                DeckDto.CreateRequest deckCreateDto2 = new DeckDto.CreateRequest(user2.id(), "User2 Deck", "Testing");
                DeckDto.GetResponse deck2 = deckService.createDeck(deckCreateDto2);

                UploadDto.CreateRequest uploadCreateDto1 = new UploadDto.CreateRequest(user1.id(), deck1.deckId(),
                                "user1_file.pdf",
                                FileType.JPEG, "https://example.com/user1_file.pdf");
                UploadDto.CreateRequest uploadCreateDto2 = new UploadDto.CreateRequest(user1.id(), deck1.deckId(),
                                "user1_image.jpg",
                                FileType.JPG, "https://example.com/user1_image.jpg");
                UploadDto.CreateRequest uploadCreateDto3 = new UploadDto.CreateRequest(user2.id(), deck2.deckId(),
                                "user2_file.png",
                                FileType.PNG, "https://example.com/user2_file.png");

                uploadService.createUpload(uploadCreateDto1, 1024L, user1.id());
                uploadService.createUpload(uploadCreateDto2, 2048L, user1.id());
                uploadService.createUpload(uploadCreateDto3, 512L, user2.id());

                List<UploadDto.GetResponse> allUploads = uploadService.getAllUploads();

                assertThat(allUploads).isNotNull();
                assertThat(allUploads).hasSize(3);
                assertThat(allUploads).extracting(UploadDto.GetResponse::fileName)
                                .containsExactlyInAnyOrder("user1_file.pdf", "user1_image.jpg", "user2_file.png");
        }

        @Test
        @WithMockUser(roles = "USER")
        void getAllUploads_NonAdmin_ThrowsUnauthorizedUploadAccessException() {
                UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith",
                                "john123",
                                "password");
                UserDto.GetResponse user = userService.createUser(userCreateDto);

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user.id().toString(),
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_USER")));
                SecurityContextHolder.getContext().setAuthentication(auth);

                try {
                        DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck",
                                        "Testing");
                        DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

                        UploadDto.CreateRequest uploadCreateDto = new UploadDto.CreateRequest(user.id(), deck.deckId(),
                                        "test-file.pdf",
                                        FileType.PNG, "https://example.com/test-file.pdf");
                        uploadService.createUpload(uploadCreateDto, 1024L, user.id());

                        UnauthorizedUploadAccessException exception = assertThrows(
                                        UnauthorizedUploadAccessException.class, () -> {
                                                uploadService.getAllUploads();
                                        });

                        assertThat(exception.getMessage()).contains("Unauthorized user access");
                } finally {
                        SecurityContextHolder.clearContext();
                }
        }

        @Test
        void getAllUserUploads_NullUserId_ThrowsInvalidUploadParameterException() {
                InvalidUploadParameterException exception = assertThrows(InvalidUploadParameterException.class, () -> {
                        uploadService.getAllUserUploads(null);
                });

                assertThat(exception.getMessage()).isEqualTo("userId cannot be null");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void getAllUserUploads_ValidInput_ReturnsUploads() {
                UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith",
                                "john123",
                                "password");
                UserDto.GetResponse user = userService.createUser(userCreateDto);

                DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");
                DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

                UploadDto.CreateRequest uploadCreateDto1 = new UploadDto.CreateRequest(user.id(), deck.deckId(),
                                "file1.PDF",
                                FileType.PNG,
                                "https://example.com/file1.PDF");
                UploadDto.CreateRequest uploadCreateDto2 = new UploadDto.CreateRequest(user.id(), deck.deckId(),
                                "file2.JPG",
                                FileType.JPG,
                                "https://example.com/file2.JPG");

                uploadService.createUpload(uploadCreateDto1, 1024L, user.id());
                uploadService.createUpload(uploadCreateDto2, 2048L, user.id());

                List<UploadDto.GetResponse> uploads = uploadService.getAllUserUploads(user.id());

                assertThat(uploads).hasSize(2);
                assertThat(uploads).extracting(UploadDto.GetResponse::fileName).containsExactlyInAnyOrder("file1.PDF",
                                "file2.JPG");
        }

        @Test
        void getAllDeckUploads_NullDeckId_ThrowsInvalidUploadParameterException() {
                InvalidUploadParameterException exception = assertThrows(InvalidUploadParameterException.class, () -> {
                        uploadService.getAllDeckUploads(null);
                });

                assertThat(exception.getMessage()).isEqualTo("deckId cannot be null");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void getAllDeckUploads_ValidInput_ReturnsUploads() {
                UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith",
                                "john123",
                                "password");
                UserDto.GetResponse user = userService.createUser(userCreateDto);

                DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");
                DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

                UploadDto.CreateRequest uploadCreateDto1 = new UploadDto.CreateRequest(user.id(), deck.deckId(),
                                "file1.PDF",
                                FileType.PNG,
                                "https://example.com/file1.PDF");
                UploadDto.CreateRequest uploadCreateDto2 = new UploadDto.CreateRequest(user.id(), deck.deckId(),
                                "file2.JPG",
                                FileType.JPG,
                                "https://example.com/file2.JPG");

                uploadService.createUpload(uploadCreateDto1, 1024L, user.id());
                uploadService.createUpload(uploadCreateDto2, 2048L, user.id());

                List<UploadDto.GetResponse> uploads = uploadService.getAllDeckUploads(deck.deckId());

                assertThat(uploads).hasSize(2);
                assertThat(uploads).extracting(UploadDto.GetResponse::fileName).containsExactlyInAnyOrder("file1.PDF",
                                "file2.JPG");
        }

        @Test
        void getCountOfAllUserUploads_NullUserId_ThrowsInvalidUploadParameterException() {
                InvalidUploadParameterException exception = assertThrows(InvalidUploadParameterException.class, () -> {
                        uploadService.getCountOfAllUserUploads(null);
                });

                assertThat(exception.getMessage()).isEqualTo("userId cannot be null");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void getCountOfAllUserUploads_ValidInput_ReturnsCount() {
                UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith",
                                "john123",
                                "password");
                UserDto.GetResponse user = userService.createUser(userCreateDto);

                DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");
                DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

                UploadDto.CreateRequest uploadCreateDto1 = new UploadDto.CreateRequest(user.id(), deck.deckId(),
                                "file1.PDF",
                                FileType.PNG,
                                "https://example.com/file1.PDF");
                UploadDto.CreateRequest uploadCreateDto2 = new UploadDto.CreateRequest(user.id(), deck.deckId(),
                                "file2.JPG",
                                FileType.JPG,
                                "https://example.com/file2.JPG");

                uploadService.createUpload(uploadCreateDto1, 1024L, user.id());
                uploadService.createUpload(uploadCreateDto2, 2048L, user.id());

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
        @WithMockUser(roles = "ADMIN")
        void getCountOfAllDeckUploads_ValidInput_ReturnsCount() {
                UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith",
                                "john123",
                                "password");
                UserDto.GetResponse user = userService.createUser(userCreateDto);

                DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");
                DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

                UploadDto.CreateRequest uploadCreateDto1 = new UploadDto.CreateRequest(user.id(), deck.deckId(),
                                "file1.PDF",
                                FileType.PNG,
                                "https://example.com/file1.PDF");
                UploadDto.CreateRequest uploadCreateDto2 = new UploadDto.CreateRequest(user.id(), deck.deckId(),
                                "file2.JPG",
                                FileType.JPG,
                                "https://example.com/file2.JPG");

                uploadService.createUpload(uploadCreateDto1, 1024L, user.id());
                uploadService.createUpload(uploadCreateDto2, 2048L, user.id());

                long count = uploadService.getCountOfAllDeckUploads(deck.deckId());

                assertThat(count).isEqualTo(2);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void startParsingUpload_ValidInput_ReturnsUpload() {
                UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith",
                                "john123",
                                "password");
                UserDto.GetResponse user = userService.createUser(userCreateDto);

                DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");
                DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

                UploadDto.CreateRequest uploadCreateDto = new UploadDto.CreateRequest(user.id(), deck.deckId(),
                                "test-file.PDF",
                                FileType.PNG, "https://example.com/file.PDF");
                UploadDto.GetResponse upload = uploadService.createUpload(uploadCreateDto, 1024L, user.id());

                UploadDto.GetResponse updatedUpload = uploadService.startParsingUpload(upload.id(), user.id());

                assertThat(updatedUpload.parsingStatus()).isEqualTo(ParsingStatus.PROCESSING);
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
        @WithMockUser(roles = "ADMIN")
        void startParsingUpload_NullRequestingUserId_ThrowsInvalidUploadParameterException() {
                UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith",
                                "john123",
                                "password");
                UserDto.GetResponse user = userService.createUser(userCreateDto);

                DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");
                DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

                UploadDto.CreateRequest uploadCreateDto = new UploadDto.CreateRequest(user.id(), deck.deckId(),
                                "test-file.PDF",
                                FileType.PNG, "https://example.com/file.PDF");
                UploadDto.GetResponse upload = uploadService.createUpload(uploadCreateDto, 1024L, user.id());

                InvalidUploadParameterException exception = assertThrows(InvalidUploadParameterException.class, () -> {
                        uploadService.startParsingUpload(upload.id(), null);
                });

                assertThat(exception.getMessage()).isEqualTo("Requesting userId cannot be null");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void startParsingUpload_UnauthorizedUser_ThrowsUnauthorizedUploadAccessException() {
                UserDto.CreateRequest userCreateDto1 = new UserDto.CreateRequest("test1@email.com", "John Smith",
                                "john123",
                                "password");
                UserDto.GetResponse user1 = userService.createUser(userCreateDto1);

                UserDto.CreateRequest userCreateDto2 = new UserDto.CreateRequest("test2@email.com", "Jane Doe",
                                "jane123",
                                "password");
                UserDto.GetResponse user2 = userService.createUser(userCreateDto2);

                DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user1.id(), "Test Deck", "Testing");
                DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

                UploadDto.CreateRequest uploadCreateDto = new UploadDto.CreateRequest(user1.id(), deck.deckId(),
                                "test-file.PDF",
                                FileType.PNG, "https://example.com/file.PDF");
                UploadDto.GetResponse upload = uploadService.createUpload(uploadCreateDto, 1024L, user1.id());

                UnauthorizedUploadAccessException exception = assertThrows(UnauthorizedUploadAccessException.class,
                                () -> {
                                        uploadService.startParsingUpload(upload.id(), user2.id());
                                });

                assertThat(exception.getMessage()).isEqualTo("You can only modify your own uploads");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void completeParsingUpload_ValidInput_ReturnsUpload() {
                UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith",
                                "john123",
                                "password");
                UserDto.GetResponse user = userService.createUser(userCreateDto);

                DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");
                DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

                UploadDto.CreateRequest uploadCreateDto = new UploadDto.CreateRequest(user.id(), deck.deckId(),
                                "test-file.PDF",
                                FileType.PNG, "https://example.com/file.PDF");
                UploadDto.GetResponse upload = uploadService.createUpload(uploadCreateDto, 1024L, user.id());

                UploadDto.GetResponse startedParsingUpload = uploadService.startParsingUpload(upload.id(), user.id());

                UploadDto.GetResponse finishedParsingUpload = uploadService.completeParsingUpload(
                                startedParsingUpload.id(),
                                user.id());

                assertThat(finishedParsingUpload.parsingStatus()).isEqualTo(ParsingStatus.COMPLETED);
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
        @WithMockUser(roles = "ADMIN")
        void completeParsingUpload_NullRequestingUserId_ThrowsInvalidUploadParameterException() {
                UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith",
                                "john123",
                                "password");
                UserDto.GetResponse user = userService.createUser(userCreateDto);

                DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");
                DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

                UploadDto.CreateRequest uploadCreateDto = new UploadDto.CreateRequest(user.id(), deck.deckId(),
                                "test-file.PDF",
                                FileType.PNG, "https://example.com/file.PDF");
                UploadDto.GetResponse upload = uploadService.createUpload(uploadCreateDto, 1024L, user.id());

                InvalidUploadParameterException exception = assertThrows(InvalidUploadParameterException.class, () -> {
                        uploadService.completeParsingUpload(upload.id(), null);
                });

                assertThat(exception.getMessage()).isEqualTo("Requesting userId cannot be null");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void completeParsingUpload_UnauthorizedUser_ThrowsUnauthorizedUploadAccessException() {
                UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith",
                                "john123",
                                "password");
                UserDto.GetResponse user = userService.createUser(userCreateDto);

                UserDto.CreateRequest userCreateDto2 = new UserDto.CreateRequest("test2@email.com", "Jane Doe",
                                "jane123",
                                "password");
                UserDto.GetResponse user2 = userService.createUser(userCreateDto2);

                DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");
                DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

                UploadDto.CreateRequest uploadCreateDto = new UploadDto.CreateRequest(user.id(), deck.deckId(),
                                "test-file.PDF",
                                FileType.PNG, "https://example.com/file.PDF");
                UploadDto.GetResponse upload = uploadService.createUpload(uploadCreateDto, 1024L, user.id());

                UploadDto.GetResponse startedParsingUpload = uploadService.startParsingUpload(upload.id(), user.id());

                UnauthorizedUploadAccessException exception = assertThrows(UnauthorizedUploadAccessException.class,
                                () -> {
                                        uploadService.completeParsingUpload(startedParsingUpload.id(), user2.id());
                                });

                assertThat(exception.getMessage()).isEqualTo("You can only modify your own uploads");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void failParsingUpload_ValidInput_ReturnsUpload() {
                UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith",
                                "john123",
                                "password");
                UserDto.GetResponse user = userService.createUser(userCreateDto);

                DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");
                DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

                UploadDto.CreateRequest uploadCreateDto = new UploadDto.CreateRequest(user.id(), deck.deckId(),
                                "test-file.PDF",
                                FileType.PNG, "https://example.com/file.PDF");
                UploadDto.GetResponse upload = uploadService.createUpload(uploadCreateDto, 1024L, user.id());

                UploadDto.GetResponse startedParsingUpload = uploadService.startParsingUpload(upload.id(), user.id());

                UploadDto.GetResponse failParsingUpload = uploadService.failParsingUpload(startedParsingUpload.id(),
                                "Issue parsing this upload", user.id());

                assertThat(failParsingUpload.errorMessage()).isEqualTo("Issue parsing this upload");
                assertThat(failParsingUpload.parsingStatus()).isEqualTo(ParsingStatus.FAILED);
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
        @WithMockUser(roles = "ADMIN")
        void failParsingUpload_NullErrorMessage_ThrowsInvalidUploadParameterException() {
                UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith",
                                "john123",
                                "password");
                UserDto.GetResponse user = userService.createUser(userCreateDto);

                DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");
                DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

                UploadDto.CreateRequest uploadCreateDto = new UploadDto.CreateRequest(user.id(), deck.deckId(),
                                "test-file.PDF",
                                FileType.PNG, "https://example.com/file.PDF");
                UploadDto.GetResponse upload = uploadService.createUpload(uploadCreateDto, 1024L, user.id());

                InvalidUploadParameterException exception = assertThrows(InvalidUploadParameterException.class, () -> {
                        uploadService.failParsingUpload(upload.id(), null, user.id());
                });

                assertThat(exception.getMessage()).isEqualTo("Error message cannot be null");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void failParsingUpload_NullRequestingUserId_ThrowsInvalidUploadParameterException() {
                UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith",
                                "john123",
                                "password");
                UserDto.GetResponse user = userService.createUser(userCreateDto);

                DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");
                DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

                UploadDto.CreateRequest uploadCreateDto = new UploadDto.CreateRequest(user.id(), deck.deckId(),
                                "test-file.PDF",
                                FileType.PNG, "https://example.com/file.PDF");
                UploadDto.GetResponse upload = uploadService.createUpload(uploadCreateDto, 1024L, user.id());

                InvalidUploadParameterException exception = assertThrows(InvalidUploadParameterException.class, () -> {
                        uploadService.failParsingUpload(upload.id(), "Error message", null);
                });

                assertThat(exception.getMessage()).isEqualTo("Requesting userId cannot be null");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void failParsingUpload_UnauthorizedUser_ThrowsUnauthorizedUploadAccessException() {
                UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith",
                                "john123",
                                "password");
                UserDto.GetResponse user = userService.createUser(userCreateDto);

                UserDto.CreateRequest userCreateDto2 = new UserDto.CreateRequest("test2@email.com", "Jane Doe",
                                "jane123",
                                "password");
                UserDto.GetResponse user2 = userService.createUser(userCreateDto2);

                DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");
                DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

                UploadDto.CreateRequest uploadCreateDto = new UploadDto.CreateRequest(user.id(), deck.deckId(),
                                "test-file.PDF",
                                FileType.PNG, "https://example.com/file.PDF");
                UploadDto.GetResponse upload = uploadService.createUpload(uploadCreateDto, 1024L, user.id());

                UploadDto.GetResponse startedParsingUpload = uploadService.startParsingUpload(upload.id(), user.id());

                UnauthorizedUploadAccessException exception = assertThrows(UnauthorizedUploadAccessException.class,
                                () -> {
                                        uploadService.failParsingUpload(startedParsingUpload.id(),
                                                        "Issue parsing this upload", user2.id());
                                });

                assertThat(exception.getMessage()).isEqualTo("You can only modify your own uploads");
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
        @WithMockUser(roles = "ADMIN")
        void deleteUploadById_NullRequestingUserId_ThrowsInvalidUploadParameterException() {
                UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith",
                                "john123",
                                "password");
                UserDto.GetResponse user = userService.createUser(userCreateDto);

                DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");
                DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

                UploadDto.CreateRequest uploadCreateDto = new UploadDto.CreateRequest(user.id(), deck.deckId(),
                                "test-file.PDF",
                                FileType.PNG, "https://example.com/file.PDF");
                UploadDto.GetResponse upload = uploadService.createUpload(uploadCreateDto, 1024L, user.id());

                InvalidUploadParameterException exception = assertThrows(InvalidUploadParameterException.class, () -> {
                        uploadService.deleteUploadById(upload.id(), null);
                });

                assertThat(exception.getMessage()).isEqualTo("Requesting userId cannot be null");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void deleteUploadById_UnauthorizedUser_ThrowsUnauthorizedUploadAccessException() {
                UserDto.CreateRequest userCreateDto1 = new UserDto.CreateRequest("test1@email.com", "John Smith",
                                "john123",
                                "password");
                UserDto.GetResponse user1 = userService.createUser(userCreateDto1);

                UserDto.CreateRequest userCreateDto2 = new UserDto.CreateRequest("test2@email.com", "Jane Doe",
                                "jane123",
                                "password");
                UserDto.GetResponse user2 = userService.createUser(userCreateDto2);

                DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user1.id(), "Test Deck", "Testing");
                DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

                UploadDto.CreateRequest uploadCreateDto = new UploadDto.CreateRequest(user1.id(), deck.deckId(),
                                "test-file.PDF",
                                FileType.PNG, "https://example.com/file.PDF");
                UploadDto.GetResponse upload = uploadService.createUpload(uploadCreateDto, 1024L, user1.id());

                UnauthorizedUploadAccessException exception = assertThrows(UnauthorizedUploadAccessException.class,
                                () -> {
                                        uploadService.deleteUploadById(upload.id(), user2.id());
                                });

                assertThat(exception.getMessage()).isEqualTo("You can only delete your own uploads");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void deleteUploadById_ValidInput_DeletesUpload() {
                UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith",
                                "john123",
                                "password");
                UserDto.GetResponse user = userService.createUser(userCreateDto);

                DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");
                DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

                UploadDto.CreateRequest uploadCreateDto1 = new UploadDto.CreateRequest(user.id(), deck.deckId(),
                                "file1.PDF",
                                FileType.PNG,
                                "https://example.com/file1.PDF");
                UploadDto.CreateRequest uploadCreateDto2 = new UploadDto.CreateRequest(user.id(), deck.deckId(),
                                "file2.JPG",
                                FileType.JPG,
                                "https://example.com/file2.JPG");

                UploadDto.GetResponse upload1 = uploadService.createUpload(uploadCreateDto1, 1024L, user.id());
                UploadDto.GetResponse upload2 = uploadService.createUpload(uploadCreateDto2, 2048L, user.id());

                // Verify uploads exist
                assertThat(uploadService.getAllUserUploads(user.id())).hasSize(2);

                uploadService.deleteUploadById(upload1.id(), user.id());

                // Verify upload deleted
                List<UploadDto.GetResponse> remainingUploads = uploadService.getAllUserUploads(user.id());
                assertThat(remainingUploads).hasSize(1);
                assertThat(remainingUploads.get(0).id()).isEqualTo(upload2.id());

                // Verify bidirectional relationship maintained
                User refreshedUser = userRepository.findById(user.id())
                                .orElseThrow(() -> new UserNotFoundException("User not found"));
                assertThat(refreshedUser.getUploads()).hasSize(1);

                DeckDto.GetResponseWithCardsAndUploads refreshedDeck = deckService
                                .getDeckByIdWithCardsAndUploads(deck.deckId());
                assertThat(refreshedDeck.uploads()).hasSize(1);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void deleteAllUserUploads_NullUserId_ThrowsInvalidUploadParameterException() {
                InvalidUploadParameterException exception = assertThrows(InvalidUploadParameterException.class, () -> {
                        uploadService.deleteAllUserUploads(null);
                });

                assertThat(exception.getMessage()).isEqualTo("userId cannot be null");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void deleteAllUserUploads_NonExistentUser_ThrowsUserNotFoundException() {
                UUID nonExistentUserId = UUID.randomUUID();

                UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
                        uploadService.deleteAllUserUploads(nonExistentUserId);
                });

                assertThat(exception.getMessage()).contains("User not found");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void deleteAllUserUploads_ValidInput_DeletesAllUploads() {
                UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith",
                                "john123",
                                "password");
                UserDto.GetResponse user = userService.createUser(userCreateDto);

                DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");
                DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

                UploadDto.CreateRequest uploadCreateDto1 = new UploadDto.CreateRequest(user.id(), deck.deckId(),
                                "file1.PDF",
                                FileType.PNG,
                                "https://example.com/file1.PDF");
                UploadDto.CreateRequest uploadCreateDto2 = new UploadDto.CreateRequest(user.id(), deck.deckId(),
                                "file2.JPG",
                                FileType.JPG,
                                "https://example.com/file2.JPG");

                uploadService.createUpload(uploadCreateDto1, 1024L, user.id());
                uploadService.createUpload(uploadCreateDto2, 2048L, user.id());

                assertThat(uploadService.getAllUserUploads(user.id())).hasSize(2);

                uploadService.deleteAllUserUploads(user.id());

                assertThat(uploadService.getAllUserUploads(user.id())).hasSize(0);

                User refreshedUser = userRepository.findById(user.id())
                                .orElseThrow(() -> new UserNotFoundException("User not found"));
                assertThat(refreshedUser.getUploads()).hasSize(0);

                DeckDto.GetResponseWithCardsAndUploads refreshedDeck = deckService
                                .getDeckByIdWithCardsAndUploads(deck.deckId());
                assertThat(refreshedDeck.uploads()).hasSize(0);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void deleteAllUserUploads_NonAdmin_ThrowsUnauthorizedUploadAccessException() {
                UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith",
                                "john123",
                                "password");
                UserDto.GetResponse user = userService.createUser(userCreateDto);

                DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");
                DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);
                DeckDto.CreateRequest deckCreateDto2 = new DeckDto.CreateRequest(user.id(), "Test Deck 2", "Testing 2");
                DeckDto.GetResponse deck2 = deckService.createDeck(deckCreateDto2);

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user.id().toString(),
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_USER")));
                SecurityContextHolder.getContext().setAuthentication(auth);

                try {
                        UploadDto.CreateRequest uploadCreateDto1 = new UploadDto.CreateRequest(user.id(), deck.deckId(),
                                        "file1.PDF", FileType.PNG,
                                        "https://example.com/file1.PDF");
                        UploadDto.CreateRequest uploadCreateDto2 = new UploadDto.CreateRequest(user.id(),
                                        deck2.deckId(),
                                        "file2.JPG", FileType.JPG,
                                        "https://example.com/file2.JPG");

                        uploadService.createUpload(uploadCreateDto1, 1024L, user.id());
                        uploadService.createUpload(uploadCreateDto2, 2048L, user.id());

                        UnauthorizedUploadAccessException exception = assertThrows(
                                        UnauthorizedUploadAccessException.class, () -> {
                                                uploadService.deleteAllUserUploads(user.id());
                                        });

                        assertThat(exception.getMessage()).isEqualTo("Admin access required");
                } finally {
                        SecurityContextHolder.clearContext();
                }
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void deleteAllDeckUploads_NullDeckId_ThrowsInvalidUploadParameterException() {
                InvalidUploadParameterException exception = assertThrows(InvalidUploadParameterException.class, () -> {
                        uploadService.deleteAllDeckUploads(null);
                });

                assertThat(exception.getMessage()).isEqualTo("deckId cannot be null");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void deleteAllDeckUploads_NonExistentDeck_ThrowsDeckNotFoundException() {
                UUID nonExistentDeckId = UUID.randomUUID();

                DeckNotFoundException exception = assertThrows(DeckNotFoundException.class, () -> {
                        uploadService.deleteAllDeckUploads(nonExistentDeckId);
                });

                assertThat(exception.getMessage()).contains("Deck with ID " + nonExistentDeckId + " not found");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void deleteAllDeckUploads_ValidInput_DeletesAllUploads() {
                UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith",
                                "john123",
                                "password");
                UserDto.GetResponse user = userService.createUser(userCreateDto);

                DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");
                DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

                UploadDto.CreateRequest uploadCreateDto1 = new UploadDto.CreateRequest(user.id(), deck.deckId(),
                                "file1.PDF",
                                FileType.PNG,
                                "https://example.com/file1.PDF");
                UploadDto.CreateRequest uploadCreateDto2 = new UploadDto.CreateRequest(user.id(), deck.deckId(),
                                "file2.JPG",
                                FileType.JPG,
                                "https://example.com/file2.JPG");

                uploadService.createUpload(uploadCreateDto1, 1024L, user.id());
                uploadService.createUpload(uploadCreateDto2, 2048L, user.id());

                assertThat(uploadService.getAllDeckUploads(deck.deckId())).hasSize(2);

                uploadService.deleteAllDeckUploads(deck.deckId());

                assertThat(uploadService.getAllDeckUploads(deck.deckId())).hasSize(0);

                User refreshedUser = userRepository.findById(user.id())
                                .orElseThrow(() -> new UserNotFoundException("User not found"));
                assertThat(refreshedUser.getUploads()).hasSize(0);

                DeckDto.GetResponseWithCardsAndUploads refreshedDeck = deckService
                                .getDeckByIdWithCardsAndUploads(deck.deckId());
                assertThat(refreshedDeck.uploads()).hasSize(0);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void deleteAllDeckUploads_NonAdmin_ThrowsUnauthorizedUploadAccessException() {
                UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith",
                                "john123",
                                "password");
                UserDto.GetResponse user = userService.createUser(userCreateDto);

                DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");
                DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user.id().toString(),
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_USER")));
                SecurityContextHolder.getContext().setAuthentication(auth);

                try {
                        UploadDto.CreateRequest uploadCreateDto1 = new UploadDto.CreateRequest(user.id(), deck.deckId(),
                                        "file1.PDF", FileType.PNG,
                                        "https://example.com/file1.PDF");
                        UploadDto.CreateRequest uploadCreateDto2 = new UploadDto.CreateRequest(user.id(), deck.deckId(),
                                        "file2.JPG", FileType.JPG,
                                        "https://example.com/file2.JPG");

                        uploadService.createUpload(uploadCreateDto1, 1024L, user.id());
                        uploadService.createUpload(uploadCreateDto2, 2048L, user.id());

                        UnauthorizedUploadAccessException exception = assertThrows(
                                        UnauthorizedUploadAccessException.class, () -> {
                                                uploadService.deleteAllDeckUploads(user.id());
                                        });

                        assertThat(exception.getMessage()).isEqualTo("Admin access required");
                } finally {
                        SecurityContextHolder.clearContext();
                }
        }
}
