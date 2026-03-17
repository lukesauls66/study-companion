package com.study_companion.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import com.study_companion.backend.dto.DeckDto;
import com.study_companion.backend.dto.UserDto;
import com.study_companion.backend.exception.deck.DeckNotFoundException;
import com.study_companion.backend.exception.deck.InvalidDeckCreationException;
import com.study_companion.backend.exception.deck.InvalidDeckParameterException;
import com.study_companion.backend.exception.deck.InvalidDeckUpdateException;
import com.study_companion.backend.exception.deck.UnauthorizedDeckAccessException;
import com.study_companion.backend.exception.user.UserNotFoundException;
import com.study_companion.backend.model.postgres.User;
import com.study_companion.backend.repository.postgres.UserRepository;

@SpringBootTest
@Transactional
@Rollback
public class DeckServiceIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeckService deckService;

    @Autowired
    private UserService userService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createDeck_ValidInput_ReturnsDeck() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");

        DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

        assertThat(deck.userId()).isEqualTo(user.id());
        assertThat(deck.title()).isEqualTo("Test Deck");
        assertThat(deck.description()).isEqualTo("Testing");
    }

    @Test
    void createDeck_NullDeckDto_ThrowsInvalidDeckParameterException() {
        InvalidDeckParameterException exception = assertThrows(InvalidDeckParameterException.class, () -> {
            deckService.createDeck(null);
        });

        assertThat(exception.getMessage()).isEqualTo("Deck data transfer object cannot be null");
    }

    @Test
    void createDeck_BlankTitle_ThrowsInvalidDeckCreationException() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), " ", "Testing");

        InvalidDeckCreationException exception = assertThrows(InvalidDeckCreationException.class, () -> {
            deckService.createDeck(deckCreateDto);
        });

        assertThat(exception.getMessage()).isEqualTo("Title cannot be empty");
    }

    @Test
    void createDeck_BlankDescription_ThrowsInvalidDeckCreationException() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", " ");

        InvalidDeckCreationException exception = assertThrows(InvalidDeckCreationException.class, () -> {
            deckService.createDeck(deckCreateDto);
        });

        assertThat(exception.getMessage()).isEqualTo("Description cannot be empty");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getDeckById_ValidInput_ReturnsDeck() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");

        DeckDto.GetResponse createdDeck = deckService.createDeck(deckCreateDto);
        DeckDto.GetResponse deck = deckService.getDeckById(createdDeck.deckId());

        assertThat(deck.title()).isEqualTo("Test Deck");
        assertThat(deck.description()).isEqualTo("Testing");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getDeckById_NonExistentId_ThrowsDeckNotFoundException() {
        UUID nonExistentId = UUID.randomUUID();

        DeckNotFoundException exception = assertThrows(DeckNotFoundException.class, () -> {
            deckService.getDeckById(nonExistentId);
        });

        assertThat(exception.getMessage()).isEqualTo("Deck with ID " + nonExistentId + " not found");
    }

    @Test
    void getDeckById_NullDeckId_ThrowsInvalidDeckParameterException() {
        InvalidDeckParameterException exception = assertThrows(InvalidDeckParameterException.class, () -> {
            deckService.getDeckById(null);
        });

        assertThat(exception.getMessage()).isEqualTo("ID cannot not be null");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllDecks_ValidInput_ReturnsDecks() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.CreateRequest deckCreateDto1 = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");
        DeckDto.CreateRequest deckCreateDto2 = new DeckDto.CreateRequest(user.id(), "Test Deck 2", "Testing 2");

        deckService.createDeck(deckCreateDto1);
        deckService.createDeck(deckCreateDto2);

        List<DeckDto.GetResponse> decks = deckService.getAllDecks();

        assertThat(decks).hasSize(2);
        assertThat(decks).extracting(DeckDto.GetResponse::title).containsExactlyInAnyOrder("Test Deck", "Test Deck 2");
        assertThat(decks).extracting(DeckDto.GetResponse::description).containsExactlyInAnyOrder("Testing",
                "Testing 2");
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllDecks_NonAdmin_ThrowsUnauthorizedDeckAccessException() {
        UnauthorizedDeckAccessException exception = assertThrows(UnauthorizedDeckAccessException.class, () -> {
            deckService.getAllDecks();
        });

        assertThat(exception.getMessage()).isEqualTo("Unauthorized user access");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUserDecks_ValidInput_ReturnsDecks() {
        UserDto.CreateRequest userCreateDto1 = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");
        UserDto.CreateRequest userCreateDto2 = new UserDto.CreateRequest("test2@email.com", "Jane Smith", "jane123",
                "password123");

        UserDto.GetResponse user1 = userService.createUser(userCreateDto1);
        UserDto.GetResponse user2 = userService.createUser(userCreateDto2);

        DeckDto.CreateRequest deckCreateDto1 = new DeckDto.CreateRequest(user1.id(), "Test Deck", "Testing");
        DeckDto.CreateRequest deckCreateDto2 = new DeckDto.CreateRequest(user2.id(), "Test Deck 2", "Testing 2");
        DeckDto.CreateRequest deckCreateDto3 = new DeckDto.CreateRequest(user1.id(), "Test Deck 3", "Testing 3");

        deckService.createDeck(deckCreateDto1);
        deckService.createDeck(deckCreateDto2);
        deckService.createDeck(deckCreateDto3);

        List<DeckDto.GetResponse> decks = deckService.getAllUserDecks(user1.id());

        assertThat(decks).hasSize(2);
        assertThat(decks).extracting(DeckDto.GetResponse::title).containsExactlyInAnyOrder("Test Deck", "Test Deck 3");
        assertThat(decks).extracting(DeckDto.GetResponse::description).containsExactlyInAnyOrder("Testing",
                "Testing 3");
    }

    @Test
    void getAllUserDecks_NullUserId_ThrowsInvalidDeckParameterException() {
        InvalidDeckParameterException exception = assertThrows(InvalidDeckParameterException.class, () -> {
            deckService.getAllUserDecks(null);
        });

        assertThat(exception.getMessage()).isEqualTo("userId cannot be null");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCountOfAllUserDecks_ValidInput_ReturnsNumOfDecks() {
        UserDto.CreateRequest userCreateDto1 = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");
        UserDto.CreateRequest userCreateDto2 = new UserDto.CreateRequest("test2@email.com", "Jane Smith", "jane123",
                "password123");

        UserDto.GetResponse user1 = userService.createUser(userCreateDto1);
        UserDto.GetResponse user2 = userService.createUser(userCreateDto2);

        DeckDto.CreateRequest deckCreateDto1 = new DeckDto.CreateRequest(user1.id(), "Test Deck", "Testing");
        DeckDto.CreateRequest deckCreateDto2 = new DeckDto.CreateRequest(user2.id(), "Test Deck 2", "Testing 2");
        DeckDto.CreateRequest deckCreateDto3 = new DeckDto.CreateRequest(user1.id(), "Test Deck 3", "Testing 3");

        deckService.createDeck(deckCreateDto1);
        deckService.createDeck(deckCreateDto2);
        deckService.createDeck(deckCreateDto3);

        long deckLength = deckService.getCountOfAllUserDecks(user1.id());

        assertThat(deckLength).isEqualTo(2);
    }

    @Test
    void getCountOfAllUserDecks_NullUserId_ThrowsInvalidDeckParameterException() {
        InvalidDeckParameterException exception = assertThrows(InvalidDeckParameterException.class, () -> {
            deckService.getCountOfAllUserDecks(null);
        });

        assertThat(exception.getMessage()).isEqualTo("userId cannot be null");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateDeck_ValidInput_ReturnsDeck() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");

        DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

        DeckDto.UpdateRequest deckUpdateDto = new DeckDto.UpdateRequest("Update Test Deck", "Update Testing");

        DeckDto.GetResponse updatedDeck = deckService.updateDeck(deck.deckId(), deckUpdateDto, user.id());

        assertThat(updatedDeck.deckId()).isEqualTo(deck.deckId());
        assertThat(updatedDeck.userId()).isEqualTo(user.id());
        assertThat(updatedDeck.title()).isEqualTo("Update Test Deck");
        assertThat(updatedDeck.description()).isEqualTo("Update Testing");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateDeck_InvalidInput_ThrowsInvalidDeckUpdateException() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");

        DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

        DeckDto.UpdateRequest deckUpdateDto = new DeckDto.UpdateRequest("", "");

        InvalidDeckUpdateException exception = assertThrows(InvalidDeckUpdateException.class, () -> {
            deckService.updateDeck(deck.deckId(), deckUpdateDto, user.id());
        });

        assertThat(exception.getMessage()).isEqualTo("At least one field must be provided for update");
    }

    @Test
    void updateDeck_NullDeckId_ThrowsInvalidDeckParameterException() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.UpdateRequest deckUpdateDto = new DeckDto.UpdateRequest("", "");

        InvalidDeckParameterException exception = assertThrows(InvalidDeckParameterException.class, () -> {
            deckService.updateDeck(null, deckUpdateDto, user.id());
        });

        assertThat(exception.getMessage()).isEqualTo("deckId cannot be null");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateDeck_NullDeckDto_ThrowsInvalidDeckParameterException() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");

        DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

        InvalidDeckParameterException exception = assertThrows(InvalidDeckParameterException.class, () -> {
            deckService.updateDeck(deck.deckId(), null, user.id());
        });

        assertThat(exception.getMessage()).isEqualTo("Deck data transfer object cannot be null");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateDeck_NullRequestingUserId_ThrowsInvalidDeckParameterException() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");

        DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

        DeckDto.UpdateRequest deckUpdateDto = new DeckDto.UpdateRequest("", "");

        InvalidDeckParameterException exception = assertThrows(InvalidDeckParameterException.class, () -> {
            deckService.updateDeck(deck.deckId(), deckUpdateDto, null);
        });

        assertThat(exception.getMessage()).isEqualTo("Requesting userId cannot be null");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateDeck_InvalidUserAccess_ThrowsUnauthorizedDeckAccessException() {
        UUID unauthorizedUUID = UUID.randomUUID();
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");

        DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

        DeckDto.UpdateRequest deckUpdateDto = new DeckDto.UpdateRequest("Update Test Deck", "Update Testing");

        UnauthorizedDeckAccessException exception = assertThrows(UnauthorizedDeckAccessException.class, () -> {
            deckService.updateDeck(deck.deckId(), deckUpdateDto, unauthorizedUUID);
        });

        assertThat(exception.getMessage()).isEqualTo("You can only update your own decks");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteDeckById_ValidInput() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");

        DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

        User refreshedUser1 = userRepository.findById(user.id())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        assertThat(refreshedUser1.getDecks()).hasSize(1);

        deckService.deleteDeckById(deck.deckId(), user.id());

        User refreshedUser2 = userRepository.findById(user.id())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        assertThat(refreshedUser2.getDecks()).hasSize(0);
    }

    @Test
    void deleteDeckById_NullDeckId_ThrowsInvalidDeckParameterException() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        InvalidDeckParameterException exception = assertThrows(InvalidDeckParameterException.class, () -> {
            deckService.deleteDeckById(null, user.id());
        });

        assertThat(exception.getMessage()).isEqualTo("deckId cannot be null");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteDeckById_NullRequestingUserId_ThrowsInvalidDeckParameterException() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");

        DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

        InvalidDeckParameterException exception = assertThrows(InvalidDeckParameterException.class, () -> {
            deckService.deleteDeckById(deck.deckId(), null);
        });

        assertThat(exception.getMessage()).isEqualTo("Requesting userId cannot be null");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteDeckById_InvalidUserAccess_ThrowsUnauthorizedDeckAccessException() {
        UUID unauthorizedUUID = UUID.randomUUID();
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");

        DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

        UnauthorizedDeckAccessException exception = assertThrows(UnauthorizedDeckAccessException.class, () -> {
            deckService.deleteDeckById(deck.deckId(), unauthorizedUUID);
        });

        assertThat(exception.getMessage()).isEqualTo("You can only delete your own decks");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteAllUserDecks_ValidInput() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.CreateRequest deckCreateDto1 = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");
        DeckDto.CreateRequest deckCreateDto2 = new DeckDto.CreateRequest(user.id(), "Test Deck 2", "Testing 2");
        DeckDto.CreateRequest deckCreateDto3 = new DeckDto.CreateRequest(user.id(), "Test Deck 3", "Testing 3");

        deckService.createDeck(deckCreateDto1);
        deckService.createDeck(deckCreateDto2);
        deckService.createDeck(deckCreateDto3);

        User refreshedUser1 = userRepository.findById(user.id())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        assertThat(refreshedUser1.getDecks()).hasSize(3);

        deckService.deleteAllUserDecks(user.id());

        User refreshedUser2 = userRepository.findById(user.id())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        assertThat(refreshedUser2.getDecks()).hasSize(0);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteAllUserDecks_NullUserId_ThrowsInvalidDeckParameterException() {
        InvalidDeckParameterException exception = assertThrows(InvalidDeckParameterException.class, () -> {
            deckService.deleteAllUserDecks(null);
        });

        assertThat(exception.getMessage()).isEqualTo("userId cannot be null");
    }

    @Test
    void deleteAllUserDecks_NonAdminAndNotOwner_ThrowsUnauthorizedDeckAccessException() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user.id().toString(), null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        try {
            DeckDto.CreateRequest deckCreateDto1 = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");
            DeckDto.CreateRequest deckCreateDto2 = new DeckDto.CreateRequest(user.id(), "Test Deck 2", "Testing 2");
            DeckDto.CreateRequest deckCreateDto3 = new DeckDto.CreateRequest(user.id(), "Test Deck 3", "Testing 3");

            deckService.createDeck(deckCreateDto1);
            deckService.createDeck(deckCreateDto2);
            deckService.createDeck(deckCreateDto3);

            UnauthorizedDeckAccessException exception = assertThrows(UnauthorizedDeckAccessException.class, () -> {
                deckService.deleteAllUserDecks(UUID.randomUUID());
            });

            assertThat(exception.getMessage()).isEqualTo("Unauthorized user access");
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
