package com.lukesauls.studycompanion.studycompanion_backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import com.lukesauls.studycompanion.studycompanion_backend.dto.DeckDto;
import com.lukesauls.studycompanion.studycompanion_backend.dto.UserDto;
import com.lukesauls.studycompanion.studycompanion_backend.exception.DeckNotFoundException;
import com.lukesauls.studycompanion.studycompanion_backend.exception.InvalidDeckCreationException;
import com.lukesauls.studycompanion.studycompanion_backend.exception.InvalidDeckUpdateException;
import com.lukesauls.studycompanion.studycompanion_backend.exception.UnauthorizedDeckAccessException;
import com.lukesauls.studycompanion.studycompanion_backend.model.postgres.Deck;
import com.lukesauls.studycompanion.studycompanion_backend.model.postgres.User;


@SpringBootTest
@Transactional
@Rollback
public class DeckServiceIntegrationTest {
    
    @Autowired
    private DeckService deckService;

    @Autowired
    private UserService userService;

    @Test
    void createDeck_ValidInput_ReturnsDeck() {
        UserDto.Create userCreateDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");

        User user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.getId(), "Test Deck", "Testing");

        Deck deck = deckService.createDeck(deckCreateDto);

        assertThat(deck.getUser().getId()).isEqualTo(user.getId());
        assertThat(deck.getTitle()).isEqualTo("Test Deck");
        assertThat(deck.getDescription()).isEqualTo("Testing");
    }

    @Test
    void createDeck_BlankTitle_ThrowsInvalidDeckCreationException() {
        UserDto.Create userCreateDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");

        User user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.getId(), " ", "Testing");

        InvalidDeckCreationException exception = assertThrows(InvalidDeckCreationException.class, () -> {
            deckService.createDeck(deckCreateDto);
        });

        assertThat(exception.getMessage()).isEqualTo("Title cannot be empty");
    }

    @Test
    void createDeck_BlankDescription_ThrowsInvalidDeckCreationException() {
        UserDto.Create userCreateDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");

        User user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.getId(), "Test Deck", " ");

        InvalidDeckCreationException exception = assertThrows(InvalidDeckCreationException.class, () -> {
            deckService.createDeck(deckCreateDto);
        });

        assertThat(exception.getMessage()).isEqualTo("Description cannot be empty");
    }

    @Test
    @SuppressWarnings("null")
    void getDeckById_ValidInput_ReturnsDeck() {
        UserDto.Create userCreateDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");

        User user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.getId(), "Test Deck", "Testing");

        Deck createdDeck = deckService.createDeck(deckCreateDto);
        Deck deck = deckService.getDeckById(createdDeck.getId());

        assertThat(deck.getTitle()).isEqualTo("Test Deck");
        assertThat(deck.getDescription()).isEqualTo("Testing");
    }

    @Test
    @SuppressWarnings("null")
    void getDeckById_NonExistentId_ThrowsDeckNotFoundException() {
        UUID nonExistentId = UUID.randomUUID();

        DeckNotFoundException exception = assertThrows(DeckNotFoundException.class, () -> {
            deckService.getDeckById(nonExistentId);
        });

        assertThat(exception.getMessage()).isEqualTo("Deck with ID " + nonExistentId + " not found");
    }

    @Test
    void getAllDecks_ReturnsDecks() {
        UserDto.Create userCreateDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");

        User user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto1 = new DeckDto.Create(user.getId(), "Test Deck", "Testing");
        DeckDto.Create deckCreateDto2 = new DeckDto.Create(user.getId(), "Test Deck 2", "Testing 2");

        deckService.createDeck(deckCreateDto1);
        deckService.createDeck(deckCreateDto2);

        List<Deck> decks = deckService.getAllDecks();

        assertThat(decks).hasSize(2);
        assertThat(decks).extracting(Deck::getTitle).containsExactlyInAnyOrder("Test Deck", "Test Deck 2");
        assertThat(decks).extracting(Deck::getDescription).containsExactlyInAnyOrder("Testing", "Testing 2");
    }

    @Test
    @SuppressWarnings("null")
    void getAllUserDecks_ReturnsDecks() {
        UserDto.Create userCreateDto1 = new UserDto.Create("test@email.com", "John Smith", "john123", "password");
        UserDto.Create userCreateDto2 = new UserDto.Create("test2@email.com", "Jane Smith", "jane123", "password123");

        User user1 = userService.createUser(userCreateDto1);
        User user2 = userService.createUser(userCreateDto2);

        DeckDto.Create deckCreateDto1 = new DeckDto.Create(user1.getId(), "Test Deck", "Testing");
        DeckDto.Create deckCreateDto2 = new DeckDto.Create(user2.getId(), "Test Deck 2", "Testing 2");
        DeckDto.Create deckCreateDto3 = new DeckDto.Create(user1.getId(), "Test Deck 3", "Testing 3");

        deckService.createDeck(deckCreateDto1);
        deckService.createDeck(deckCreateDto2);
        deckService.createDeck(deckCreateDto3);

        List<Deck> decks = deckService.getAllUserDecks(user1.getId());

        assertThat(decks).hasSize(2);
        assertThat(decks).extracting(Deck::getTitle).containsExactlyInAnyOrder("Test Deck", "Test Deck 3");
        assertThat(decks).extracting(Deck::getDescription).containsExactlyInAnyOrder("Testing", "Testing 3");
    }

    @Test
    @SuppressWarnings("null")
    void countAllUserDecks_ReturnsNumOfDecks() {
        UserDto.Create userCreateDto1 = new UserDto.Create("test@email.com", "John Smith", "john123", "password");
        UserDto.Create userCreateDto2 = new UserDto.Create("test2@email.com", "Jane Smith", "jane123", "password123");

        User user1 = userService.createUser(userCreateDto1);
        User user2 = userService.createUser(userCreateDto2);

        DeckDto.Create deckCreateDto1 = new DeckDto.Create(user1.getId(), "Test Deck", "Testing");
        DeckDto.Create deckCreateDto2 = new DeckDto.Create(user2.getId(), "Test Deck 2", "Testing 2");
        DeckDto.Create deckCreateDto3 = new DeckDto.Create(user1.getId(), "Test Deck 3", "Testing 3");

        deckService.createDeck(deckCreateDto1);
        deckService.createDeck(deckCreateDto2);
        deckService.createDeck(deckCreateDto3);

        long deckLength = deckService.getCountOfAllUserDecks(user1.getId());

        assertThat(deckLength).isEqualTo(2);
    }

    @Test
    @SuppressWarnings("null")
    void updateDeck_ValidInput_ReturnsDeck() {
        UserDto.Create userCreateDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");

        User user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.getId(), "Test Deck", "Testing");

        Deck deck = deckService.createDeck(deckCreateDto);

        DeckDto.Update deckUpdateDto = new DeckDto.Update("Update Test Deck", "Update Testing");

        Deck updatedDeck = deckService.updateDeck(deck.getId(), deckUpdateDto, user.getId());

        assertThat(updatedDeck.getId()).isEqualTo(deck.getId());
        assertThat(updatedDeck.getUser().getId()).isEqualTo(user.getId());
        assertThat(updatedDeck.getTitle()).isEqualTo("Update Test Deck");
        assertThat(updatedDeck.getDescription()).isEqualTo("Update Testing");
    }

    @Test
    @SuppressWarnings("null")
    void updateDeck_InvalidInput_ThrowsInvalidDeckUpdateException() {
        UserDto.Create userCreateDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");

        User user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.getId(), "Test Deck", "Testing");

        Deck deck = deckService.createDeck(deckCreateDto);

        DeckDto.Update deckUpdateDto = new DeckDto.Update("", "");

        InvalidDeckUpdateException exception = assertThrows(InvalidDeckUpdateException.class, () -> {
            deckService.updateDeck(deck.getId(), deckUpdateDto, user.getId());
        });

        assertThat(exception.getMessage()).isEqualTo("At least one field must be provided for update");
    }

    @Test
    @SuppressWarnings("null")
    void updateDeck_InvalidUserAccess_ThrowsUnauthorizedDeckAccessException() {
        UUID unathorizedUUID = UUID.randomUUID();
        UserDto.Create userCreateDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");

        User user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.getId(), "Test Deck", "Testing");

        Deck deck = deckService.createDeck(deckCreateDto);

        DeckDto.Update deckUpdateDto = new DeckDto.Update("Update Test Deck", "Update Testing");

        UnauthorizedDeckAccessException exception = assertThrows(UnauthorizedDeckAccessException.class, () -> {
            deckService.updateDeck(deck.getId(), deckUpdateDto, unathorizedUUID);
        });

        assertThat(exception.getMessage()).isEqualTo("You can only update your own decks");
    }

    @Test
    @SuppressWarnings("null")
    void deleteDeckById_ValidInput() {
        UserDto.Create userCreateDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");

        User user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.getId(), "Test Deck", "Testing");

        Deck deck = deckService.createDeck(deckCreateDto);

        User refreshedUser1 = userService.getUserById(user.getId());
        assertThat(refreshedUser1.getDecks()).hasSize(1);

        deckService.deleteDeckById(deck.getId(), user.getId());

        User refreshedUser2 = userService.getUserById(user.getId());
        assertThat(refreshedUser2.getDecks()).hasSize(0);
    }

    @Test
    @SuppressWarnings("null")
    void deleteDeckById_InvalidUserAccess_ThrowsUnauthorizedDeckAccessException() {
        UUID unauthorizedUUID = UUID.randomUUID();
        UserDto.Create userCreateDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");

        User user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.getId(), "Test Deck", "Testing");

        Deck deck = deckService.createDeck(deckCreateDto);

        UnauthorizedDeckAccessException exception = assertThrows(UnauthorizedDeckAccessException.class, () -> {
            deckService.deleteDeckById(deck.getId(), unauthorizedUUID);
        });

        assertThat(exception.getMessage()).isEqualTo("You can only delete your own decks");
    }

    @Test
    @SuppressWarnings("null")
    void deleteAllUserDecks() {
        UserDto.Create userCreateDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");

        User user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto1 = new DeckDto.Create(user.getId(), "Test Deck", "Testing");
        DeckDto.Create deckCreateDto2 = new DeckDto.Create(user.getId(), "Test Deck 2", "Testing 2");
        DeckDto.Create deckCreateDto3 = new DeckDto.Create(user.getId(), "Test Deck 3", "Testing 3");

        deckService.createDeck(deckCreateDto1);
        deckService.createDeck(deckCreateDto2);
        deckService.createDeck(deckCreateDto3);

        User refreshedUser1 = userService.getUserById(user.getId());
        assertThat(refreshedUser1.getDecks()).hasSize(3);

        deckService.deleteAllUserDecks(user.getId());

        User refreshedUser2 = userService.getUserById(user.getId());
        assertThat(refreshedUser2.getDecks()).hasSize(0);
    }
}
