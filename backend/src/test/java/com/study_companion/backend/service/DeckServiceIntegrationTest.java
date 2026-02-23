package com.study_companion.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
import com.study_companion.backend.model.postgres.Deck;
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
    void createDeck_ValidInput_ReturnsDeck() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123", "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.id(), "Test Deck", "Testing");

        Deck deck = deckService.createDeck(deckCreateDto);

        assertThat(deck.getUser().getId()).isEqualTo(user.id());
        assertThat(deck.getTitle()).isEqualTo("Test Deck");
        assertThat(deck.getDescription()).isEqualTo("Testing");
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
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123", "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.id(), " ", "Testing");

        InvalidDeckCreationException exception = assertThrows(InvalidDeckCreationException.class, () -> {
            deckService.createDeck(deckCreateDto);
        });

        assertThat(exception.getMessage()).isEqualTo("Title cannot be empty");
    }

    @Test
    void createDeck_BlankDescription_ThrowsInvalidDeckCreationException() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123", "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.id(), "Test Deck", " ");

        InvalidDeckCreationException exception = assertThrows(InvalidDeckCreationException.class, () -> {
            deckService.createDeck(deckCreateDto);
        });

        assertThat(exception.getMessage()).isEqualTo("Description cannot be empty");
    }

    @Test
    void getDeckById_ValidInput_ReturnsDeck() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123", "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.id(), "Test Deck", "Testing");

        Deck createdDeck = deckService.createDeck(deckCreateDto);
        Deck deck = deckService.getDeckById(createdDeck.getId());

        assertThat(deck.getTitle()).isEqualTo("Test Deck");
        assertThat(deck.getDescription()).isEqualTo("Testing");
    }

    @Test
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
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123", "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto1 = new DeckDto.Create(user.id(), "Test Deck", "Testing");
        DeckDto.Create deckCreateDto2 = new DeckDto.Create(user.id(), "Test Deck 2", "Testing 2");

        deckService.createDeck(deckCreateDto1);
        deckService.createDeck(deckCreateDto2);

        List<Deck> decks = deckService.getAllDecks();

        assertThat(decks).hasSize(2);
        assertThat(decks).extracting(Deck::getTitle).containsExactlyInAnyOrder("Test Deck", "Test Deck 2");
        assertThat(decks).extracting(Deck::getDescription).containsExactlyInAnyOrder("Testing", "Testing 2");
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
    void getAllUserDecks_ValidInput_ReturnsDecks() {
        UserDto.CreateRequest userCreateDto1 = new UserDto.CreateRequest("test@email.com", "John Smith", "john123", "password");
        UserDto.CreateRequest userCreateDto2 = new UserDto.CreateRequest("test2@email.com", "Jane Smith", "jane123", "password123");

        UserDto.GetResponse user1 = userService.createUser(userCreateDto1);
        UserDto.GetResponse user2 = userService.createUser(userCreateDto2);

        DeckDto.Create deckCreateDto1 = new DeckDto.Create(user1.id(), "Test Deck", "Testing");
        DeckDto.Create deckCreateDto2 = new DeckDto.Create(user2.id(), "Test Deck 2", "Testing 2");
        DeckDto.Create deckCreateDto3 = new DeckDto.Create(user1.id(), "Test Deck 3", "Testing 3");

        deckService.createDeck(deckCreateDto1);
        deckService.createDeck(deckCreateDto2);
        deckService.createDeck(deckCreateDto3);

        List<Deck> decks = deckService.getAllUserDecks(user1.id());

        assertThat(decks).hasSize(2);
        assertThat(decks).extracting(Deck::getTitle).containsExactlyInAnyOrder("Test Deck", "Test Deck 3");
        assertThat(decks).extracting(Deck::getDescription).containsExactlyInAnyOrder("Testing", "Testing 3");
    }

    @Test
    void getAllUserDecks_NullUserId_ThrowsInvalidDeckParameterException() {
        InvalidDeckParameterException exception = assertThrows(InvalidDeckParameterException.class, () -> {
            deckService.getAllUserDecks(null);
        });

        assertThat(exception.getMessage()).isEqualTo("userId cannot be null");
    }

    @Test
    void getCountOfAllUserDecks_ValidInput_ReturnsNumOfDecks() {
        UserDto.CreateRequest userCreateDto1 = new UserDto.CreateRequest("test@email.com", "John Smith", "john123", "password");
        UserDto.CreateRequest userCreateDto2 = new UserDto.CreateRequest("test2@email.com", "Jane Smith", "jane123", "password123");

        UserDto.GetResponse user1 = userService.createUser(userCreateDto1);
        UserDto.GetResponse user2 = userService.createUser(userCreateDto2);

        DeckDto.Create deckCreateDto1 = new DeckDto.Create(user1.id(), "Test Deck", "Testing");
        DeckDto.Create deckCreateDto2 = new DeckDto.Create(user2.id(), "Test Deck 2", "Testing 2");
        DeckDto.Create deckCreateDto3 = new DeckDto.Create(user1.id(), "Test Deck 3", "Testing 3");

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
    void updateDeck_ValidInput_ReturnsDeck() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123", "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.id(), "Test Deck", "Testing");

        Deck deck = deckService.createDeck(deckCreateDto);

        DeckDto.Update deckUpdateDto = new DeckDto.Update("Update Test Deck", "Update Testing");

        Deck updatedDeck = deckService.updateDeck(deck.getId(), deckUpdateDto, user.id());

        assertThat(updatedDeck.getId()).isEqualTo(deck.getId());
        assertThat(updatedDeck.getUser().getId()).isEqualTo(user.id());
        assertThat(updatedDeck.getTitle()).isEqualTo("Update Test Deck");
        assertThat(updatedDeck.getDescription()).isEqualTo("Update Testing");
    }

    @Test
    void updateDeck_InvalidInput_ThrowsInvalidDeckUpdateException() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123", "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.id(), "Test Deck", "Testing");

        Deck deck = deckService.createDeck(deckCreateDto);

        DeckDto.Update deckUpdateDto = new DeckDto.Update("", "");

        InvalidDeckUpdateException exception = assertThrows(InvalidDeckUpdateException.class, () -> {
            deckService.updateDeck(deck.getId(), deckUpdateDto, user.id());
        });

        assertThat(exception.getMessage()).isEqualTo("At least one field must be provided for update");
    }

    @Test
    void updateDeck_NullDeckId_ThrowsInvalidDeckParameterException() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123", "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.Update deckUpdateDto = new DeckDto.Update("", "");

        InvalidDeckParameterException exception = assertThrows(InvalidDeckParameterException.class, () -> {
            deckService.updateDeck(null, deckUpdateDto, user.id());
        });

        assertThat(exception.getMessage()).isEqualTo("deckId cannot be null");
    }

    @Test
    void updateDeck_NullDeckDto_ThrowsInvalidDeckParameterException() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123", "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.id(), "Test Deck", "Testing");

        Deck deck = deckService.createDeck(deckCreateDto);

        InvalidDeckParameterException exception = assertThrows(InvalidDeckParameterException.class, () -> {
            deckService.updateDeck(deck.getId(), null, user.id());
        });

        assertThat(exception.getMessage()).isEqualTo("Deck data transfer object cannot be null");
    }

    @Test
    void updateDeck_NullRequestingUserId_ThrowsInvalidDeckParameterException() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123", "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.id(), "Test Deck", "Testing");

        Deck deck = deckService.createDeck(deckCreateDto);

        DeckDto.Update deckUpdateDto = new DeckDto.Update("", "");

        InvalidDeckParameterException exception = assertThrows(InvalidDeckParameterException.class, () -> {
            deckService.updateDeck(deck.getId(), deckUpdateDto, null);
        });

        assertThat(exception.getMessage()).isEqualTo("Requesting userId cannot be null");
    }

    @Test
    void updateDeck_InvalidUserAccess_ThrowsUnauthorizedDeckAccessException() {
        UUID unauthorizedUUID = UUID.randomUUID();
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123", "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.id(), "Test Deck", "Testing");

        Deck deck = deckService.createDeck(deckCreateDto);

        DeckDto.Update deckUpdateDto = new DeckDto.Update("Update Test Deck", "Update Testing");

        UnauthorizedDeckAccessException exception = assertThrows(UnauthorizedDeckAccessException.class, () -> {
            deckService.updateDeck(deck.getId(), deckUpdateDto, unauthorizedUUID);
        });

        assertThat(exception.getMessage()).isEqualTo("You can only update your own decks");
    }

    @Test
    void deleteDeckById_ValidInput() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123", "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.id(), "Test Deck", "Testing");

        Deck deck = deckService.createDeck(deckCreateDto);

        User refreshedUser1 = userRepository.findById(user.id())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        assertThat(refreshedUser1.getDecks()).hasSize(1);

        deckService.deleteDeckById(deck.getId(), user.id());

        User refreshedUser2 = userRepository.findById(user.id())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        assertThat(refreshedUser2.getDecks()).hasSize(0);
    }

    @Test
    void deleteDeckById_NullDeckId_ThrowsInvalidDeckParameterException() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123", "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        InvalidDeckParameterException exception = assertThrows(InvalidDeckParameterException.class, () -> {
            deckService.deleteDeckById(null, user.id());
        });

        assertThat(exception.getMessage()).isEqualTo("deckId cannot be null");
    }

    @Test
    void deleteDeckById_NullRequestingUserId_ThrowsInvalidDeckParameterException() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123", "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.id(), "Test Deck", "Testing");

        Deck deck = deckService.createDeck(deckCreateDto);

        InvalidDeckParameterException exception = assertThrows(InvalidDeckParameterException.class, () -> {
            deckService.deleteDeckById(deck.getId(), null);
        });

        assertThat(exception.getMessage()).isEqualTo("Requesting userId cannot be null");
    }

    @Test
    void deleteDeckById_InvalidUserAccess_ThrowsUnauthorizedDeckAccessException() {
        UUID unauthorizedUUID = UUID.randomUUID();
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123", "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.id(), "Test Deck", "Testing");

        Deck deck = deckService.createDeck(deckCreateDto);

        UnauthorizedDeckAccessException exception = assertThrows(UnauthorizedDeckAccessException.class, () -> {
            deckService.deleteDeckById(deck.getId(), unauthorizedUUID);
        });

        assertThat(exception.getMessage()).isEqualTo("You can only delete your own decks");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteAllUserDecks_ValidInput() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123", "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto1 = new DeckDto.Create(user.id(), "Test Deck", "Testing");
        DeckDto.Create deckCreateDto2 = new DeckDto.Create(user.id(), "Test Deck 2", "Testing 2");
        DeckDto.Create deckCreateDto3 = new DeckDto.Create(user.id(), "Test Deck 3", "Testing 3");

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
    @WithMockUser(roles = "USER")
    void deleteAllUserDecks_NonAdmin_ThrowsUnauthorizedDeckAccessException() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123", "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto1 = new DeckDto.Create(user.id(), "Test Deck", "Testing");
        DeckDto.Create deckCreateDto2 = new DeckDto.Create(user.id(), "Test Deck 2", "Testing 2");
        DeckDto.Create deckCreateDto3 = new DeckDto.Create(user.id(), "Test Deck 3", "Testing 3");

        deckService.createDeck(deckCreateDto1);
        deckService.createDeck(deckCreateDto2);
        deckService.createDeck(deckCreateDto3);

        UnauthorizedDeckAccessException exception = assertThrows(UnauthorizedDeckAccessException.class, () -> {
            deckService.deleteAllUserDecks(user.id());
        });

        assertThat(exception.getMessage()).isEqualTo("Unauthorized user access");
    }
}
