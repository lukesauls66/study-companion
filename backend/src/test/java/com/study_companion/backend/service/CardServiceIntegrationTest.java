package com.study_companion.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import com.study_companion.backend.dto.CardDto;
import com.study_companion.backend.dto.DeckDto;
import com.study_companion.backend.dto.UserDto;
import com.study_companion.backend.exception.card.CardNotFoundException;
import com.study_companion.backend.exception.card.InvalidCardCreationException;
import com.study_companion.backend.exception.card.InvalidCardUpdateException;
import com.study_companion.backend.exception.card.UnauthorizedCardAccessException;
import com.study_companion.backend.exception.deck.UnauthorizedDeckAccessException;
import com.study_companion.backend.model.CardCreationType;
import com.study_companion.backend.model.postgres.Card;
import com.study_companion.backend.model.postgres.Deck;
import com.study_companion.backend.model.postgres.User;

@SpringBootTest
@Transactional
@Rollback
public class CardServiceIntegrationTest {
    
    @Autowired
    private CardService cardService;

    @Autowired
    private DeckService deckService;

    @Autowired
    private UserService userService;

    @Test
    void createCard_ValidInput_ReturnsCard() {
        UserDto.Create userCreateDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");

        User user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.getId(), "Test Deck", "Testing");

        Deck deck = deckService.createDeck(deckCreateDto);

        CardDto.Create cardCreateDto1 = new CardDto.Create(deck.getId(), "Test question?", "Test answer", null);
        CardDto.Create cardCreateDto2 = new CardDto.Create(deck.getId(), "Test question 2?", "Test answer 2", "testurl.jpg");

        Card card1 = cardService.createCard(cardCreateDto1, CardCreationType.MANUAL_UPLOAD, user.getId());
        Card card2 = cardService.createCard(cardCreateDto2, CardCreationType.AI_PARSED, user.getId());

        List<Card> cards = deck.getCards();
        assertThat(card1.getDeck().getUser().getId()).isEqualTo(user.getId());
        assertThat(card2.getDeck().getUser().getId()).isEqualTo(user.getId());
        assertThat(cards).hasSize(2);
        assertThat(cards).extracting(Card::getQuestion).containsExactlyInAnyOrder("Test question?", "Test question 2?");
        assertThat(cards).extracting(Card::getAnswer).containsExactlyInAnyOrder("Test answer", "Test answer 2");
        assertThat(cards).extracting(Card::getImageUrl).containsExactlyInAnyOrder(null, "testurl.jpg");
        assertThat(cards).extracting(Card::getCreationType).containsExactlyInAnyOrder(CardCreationType.AI_PARSED, CardCreationType.MANUAL_UPLOAD);
    }

    @Test
    void createCard_InvalidUser_ThrowsUnauthorizedDeckAccessException() {
        UserDto.Create userCreateDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");

        User user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.getId(), "Test Deck", "Testing");

        Deck deck = deckService.createDeck(deckCreateDto);

        CardDto.Create cardCreateDto = new CardDto.Create(deck.getId(), "Test question?", "Test answer", null);

        UUID unauthorizedUUID = UUID.randomUUID();

        UnauthorizedDeckAccessException exception = assertThrows(UnauthorizedDeckAccessException.class, () -> {
            cardService.createCard(cardCreateDto, CardCreationType.MANUAL_UPLOAD, unauthorizedUUID);
        });

        assertThat(exception.getMessage()).isEqualTo("You can only add cards to your own decks");
    }

    @Test
    void createCard_BlankQuestion_ThrowsInvalidCardCreationException() {
        UserDto.Create userCreateDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");

        User user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.getId(), "Test Deck", "Testing");

        Deck deck = deckService.createDeck(deckCreateDto);

        CardDto.Create cardCreateDto = new CardDto.Create(deck.getId(), " ", "Test answer", null);

        InvalidCardCreationException exception = assertThrows(InvalidCardCreationException.class, () -> {
            cardService.createCard(cardCreateDto, CardCreationType.MANUAL_UPLOAD, user.getId());
        });

        assertThat(exception.getMessage()).isEqualTo("Question cannot be empty");
    }

    @Test
    void createCard_BlankAnswer_ThrowsInvalidCardCreationException() {
        UserDto.Create userCreateDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");

        User user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.getId(), "Test Deck", "Testing");

        Deck deck = deckService.createDeck(deckCreateDto);

        CardDto.Create cardCreateDto = new CardDto.Create(deck.getId(), "Test question?", " ", null);

        InvalidCardCreationException exception = assertThrows(InvalidCardCreationException.class, () -> {
            cardService.createCard(cardCreateDto, CardCreationType.MANUAL_UPLOAD, user.getId());
        });

        assertThat(exception.getMessage()).isEqualTo("Answer cannot be empty");
    }

    @Test
    void getCardById_ValidInput_ReturnsCard() {
        UserDto.Create userCreateDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");

        User user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.getId(), "Test Deck", "Testing");

        Deck deck = deckService.createDeck(deckCreateDto);

        CardDto.Create cardCreateDto = new CardDto.Create(deck.getId(), "Test question?", "Test answer", null);

        Card createdCard = cardService.createCard(cardCreateDto, CardCreationType.MANUAL_UPLOAD, user.getId());
        Card card = cardService.getCardById(createdCard.getId());

        assertThat(card.getQuestion()).isEqualTo("Test question?");
        assertThat(card.getAnswer()).isEqualTo("Test answer");
        assertThat(card.getImageUrl()).isEqualTo(null);
        assertThat(card.getCreationType()).isEqualTo(CardCreationType.MANUAL_UPLOAD);
    }

    @Test
    void getCardById_NonExistentId_ThrowsCardNotFoundException() {
        UUID nonExistentId = UUID.randomUUID();

        CardNotFoundException exception = assertThrows(CardNotFoundException.class, () -> {
            cardService.getCardById(nonExistentId);
        });

        assertThat(exception.getMessage()).isEqualTo("Card with ID " + nonExistentId + " not found");
    }

    @Test
    void getAllCards_ValidInput_ReturnsCards() {
        UserDto.Create userCreateDto1 = new UserDto.Create("test@email.com", "John Smith", "john123", "password");
        UserDto.Create userCreateDto2 = new UserDto.Create("test2@email.com", "Jane Smith", "jane123", "password2");

        User user1 = userService.createUser(userCreateDto1);
        User user2 = userService.createUser(userCreateDto2);

        DeckDto.Create deckCreateDto1 = new DeckDto.Create(user1.getId(), "Test Deck", "Testing");
        DeckDto.Create deckCreateDto2 = new DeckDto.Create(user2.getId(), "Test Deck 2", "Testing 2");

        Deck deck1 = deckService.createDeck(deckCreateDto1);
        Deck deck2 = deckService.createDeck(deckCreateDto2);

        CardDto.Create cardCreateDto1 = new CardDto.Create(deck1.getId(), "Test question?", "Test answer", null);
        CardDto.Create cardCreateDto2 = new CardDto.Create(deck1.getId(), "Test question 2?", "Test answer 2", null);
        CardDto.Create cardCreateDto3 = new CardDto.Create(deck2.getId(), "Test question 3?", "Test answer 3", "testurl.jpg");

        cardService.createCard(cardCreateDto1, CardCreationType.MANUAL_UPLOAD, user1.getId());
        cardService.createCard(cardCreateDto2, CardCreationType.MANUAL_UPLOAD, user1.getId());
        cardService.createCard(cardCreateDto3, CardCreationType.MANUAL_UPLOAD, user2.getId());

        List<Card> cards = cardService.getAllCards();

        assertThat(cards).hasSize(3);
        assertThat(cards).extracting(Card::getQuestion).containsExactlyInAnyOrder("Test question?", "Test question 2?", "Test question 3?");
        assertThat(cards).extracting(Card::getAnswer).containsExactlyInAnyOrder("Test answer", "Test answer 2", "Test answer 3");
        assertThat(cards).extracting(Card::getImageUrl).containsExactlyInAnyOrder("testurl.jpg", null, null);
        assertThat(cards).extracting(Card::getCreationType).containsExactlyInAnyOrder(CardCreationType.MANUAL_UPLOAD, CardCreationType.MANUAL_UPLOAD, CardCreationType.MANUAL_UPLOAD);
    }

    @Test
    void GetAllDeckCards_ValidInput_ReturnsCards() {
        UserDto.Create userCreateDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");

        User user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.getId(), "Test Deck", "Testing");

        Deck deck = deckService.createDeck(deckCreateDto);

        CardDto.Create cardCreateDto1 = new CardDto.Create(deck.getId(), "Test question?", "Test answer", null);
        CardDto.Create cardCreateDto2 = new CardDto.Create(deck.getId(), "Test question 2?", "Test answer 2", null);
        CardDto.Create cardCreateDto3 = new CardDto.Create(deck.getId(), "Test question 3?", "Test answer 3", "testurl.jpg");

        cardService.createCard(cardCreateDto1, CardCreationType.MANUAL_UPLOAD, user.getId());
        cardService.createCard(cardCreateDto2, CardCreationType.MANUAL_UPLOAD, user.getId());
        cardService.createCard(cardCreateDto3, CardCreationType.MANUAL_UPLOAD, user.getId());

        List<Card> cards = cardService.getAllDeckCards(deck.getId());

        assertThat(cards).hasSize(3);
        assertThat(cards).extracting(Card::getQuestion).containsExactlyInAnyOrder("Test question?", "Test question 2?", "Test question 3?");
        assertThat(cards).extracting(Card::getAnswer).containsExactlyInAnyOrder("Test answer", "Test answer 2", "Test answer 3");
        assertThat(cards).extracting(Card::getImageUrl).containsExactlyInAnyOrder("testurl.jpg", null, null);
        assertThat(cards).extracting(Card::getCreationType).containsExactlyInAnyOrder(CardCreationType.MANUAL_UPLOAD, CardCreationType.MANUAL_UPLOAD, CardCreationType.MANUAL_UPLOAD);
    }

    @Test
    void getCountOfAllDeckCards_ValidInput_ReturnsCardCount() {
        UserDto.Create userCreateDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");
 
        User user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.getId(), "Test Deck", "Testing");

        Deck deck = deckService.createDeck(deckCreateDto);

        CardDto.Create cardCreateDto1 = new CardDto.Create(deck.getId(), "Test question?", "Test answer", null);
        CardDto.Create cardCreateDto2 = new CardDto.Create(deck.getId(), "Test question 2?", "Test answer 2", null);
        CardDto.Create cardCreateDto3 = new CardDto.Create(deck.getId(), "Test question 3?", "Test answer 3", "testurl.jpg");

        cardService.createCard(cardCreateDto1, CardCreationType.MANUAL_UPLOAD, user.getId());
        cardService.createCard(cardCreateDto2, CardCreationType.MANUAL_UPLOAD, user.getId());
        cardService.createCard(cardCreateDto3, CardCreationType.MANUAL_UPLOAD, user.getId());

        long cardCount = cardService.getCountOfAllDeckCards(deck.getId());

        assertThat(cardCount).isEqualTo(3);
    }

    @Test
    void updateCard_ValidInput_ReturnsUpdatedCard() {
        UserDto.Create userCreateDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");

        User user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.getId(), "Test Deck", "Testing");

        Deck deck = deckService.createDeck(deckCreateDto);

        CardDto.Create cardCreateDto = new CardDto.Create(deck.getId(), "Test question?", "Test answer", null);

        Card card = cardService.createCard(cardCreateDto, CardCreationType.MANUAL_UPLOAD, user.getId());

        CardDto.Update cardUpdateDto = new CardDto.Update("Updated question?", "Updated answer", "updated-image.jpg");

        Card updatedCard = cardService.updateCard(card.getId(), cardUpdateDto, user.getId());

        assertThat(updatedCard.getId()).isEqualTo(card.getId());
        assertThat(updatedCard.getDeck().getUser().getId()).isEqualTo(user.getId());
        assertThat(updatedCard.getQuestion()).isEqualTo("Updated question?");
        assertThat(updatedCard.getAnswer()).isEqualTo("Updated answer");
        assertThat(updatedCard.getImageUrl()).isEqualTo("updated-image.jpg");
        assertThat(updatedCard.getCreationType()).isEqualTo(CardCreationType.MANUAL_UPLOAD);
    }

    @Test
    void updateCard_InvalidInput_ThrowsInvalidCardUpdateException() {
        UserDto.Create userCreateDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");

        User user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.getId(), "Test Deck", "Testing");

        Deck deck = deckService.createDeck(deckCreateDto);

        CardDto.Create cardCreateDto = new CardDto.Create(deck.getId(), "Test question?", "Test answer", null);

        Card card = cardService.createCard(cardCreateDto, CardCreationType.MANUAL_UPLOAD, user.getId());

        CardDto.Update cardUpdateDto = new CardDto.Update("", "", "");

        InvalidCardUpdateException exception = assertThrows(InvalidCardUpdateException.class, () -> {
            cardService.updateCard(card.getId(), cardUpdateDto, user.getId());
        });

        assertThat(exception.getMessage()).isEqualTo("At least one field must be provided");
    }

    @Test
    void updateCard_UnauthorizedUser_ThrowsUnauthorizedCardAccessException() {
        UserDto.Create userCreateDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");

        User user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.getId(), "Test Deck", "Testing");

        Deck deck = deckService.createDeck(deckCreateDto);

        CardDto.Create cardCreateDto = new CardDto.Create(deck.getId(), "Test question?", "Test answer", null);

        Card card = cardService.createCard(cardCreateDto, CardCreationType.MANUAL_UPLOAD, user.getId());

        CardDto.Update cardUpdateDto = new CardDto.Update("Updated question?", "Updated answer", "updated-image.jpg");

        UUID unauthorizedUUID = UUID.randomUUID();

        UnauthorizedCardAccessException exception = assertThrows(UnauthorizedCardAccessException.class, () -> {
            cardService.updateCard(card.getId(), cardUpdateDto, unauthorizedUUID);
        });

        assertThat(exception.getMessage()).isEqualTo("You can only update your own cards");
    }

    @Test
    void deleteCardById_ValidInput() {
        UserDto.Create userCreateDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");

        User user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.getId(), "Test Deck", "Testing");

        Deck deck = deckService.createDeck(deckCreateDto);

        CardDto.Create cardCreateDto = new CardDto.Create(deck.getId(), "Test question?", "Test answer", null);

        Card card = cardService.createCard(cardCreateDto, CardCreationType.MANUAL_UPLOAD, user.getId());

        Deck refreshedDeck1 = deckService.getDeckById(deck.getId());
        assertThat(refreshedDeck1.getCards()).hasSize(1);

        cardService.deleteCardById(card.getId(), user.getId());

        Deck refreshedDeck2 = deckService.getDeckById(deck.getId());
        assertThat(refreshedDeck2.getCards()).hasSize(0);
    }

    @Test
    void deleteCardById_UnauthorizedUser_ThrowsUnauthorizedCardAccessException() {
        UserDto.Create userCreateDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");

        User user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.getId(), "Test Deck", "Testing");

        Deck deck = deckService.createDeck(deckCreateDto);

        CardDto.Create cardCreateDto = new CardDto.Create(deck.getId(), "Test question?", "Test answer", null);

        Card card = cardService.createCard(cardCreateDto, CardCreationType.MANUAL_UPLOAD, user.getId());

        UUID unauthorizedUUID = UUID.randomUUID();

        UnauthorizedCardAccessException exception = assertThrows(UnauthorizedCardAccessException.class, () -> {
            cardService.deleteCardById(card.getId(), unauthorizedUUID);
        });

        assertThat(exception.getMessage()).isEqualTo("You can only delete your own cards");
    }

    @Test
    void deleteAllDeckCards_ValidInput() {
        UserDto.Create userCreateDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");

        User user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.getId(), "Test Deck", "Testing");

        Deck deck = deckService.createDeck(deckCreateDto);

        CardDto.Create cardCreateDto1 = new CardDto.Create(deck.getId(), "Test question?", "Test answer", null);
        CardDto.Create cardCreateDto2 = new CardDto.Create(deck.getId(), "Test question 2?", "Test answer 2", null);
        CardDto.Create cardCreateDto3 = new CardDto.Create(deck.getId(), "Test question 3?", "Test answer 3", "testurl.jpg");

        cardService.createCard(cardCreateDto1, CardCreationType.MANUAL_UPLOAD, user.getId());
        cardService.createCard(cardCreateDto2, CardCreationType.MANUAL_UPLOAD, user.getId());
        cardService.createCard(cardCreateDto3, CardCreationType.MANUAL_UPLOAD, user.getId());

        Deck refreshedDeck1 = deckService.getDeckById(deck.getId());
        assertThat(refreshedDeck1.getCards()).hasSize(3);

        cardService.deleteAllDeckCards(deck.getId());

        Deck refreshedDeck2 = deckService.getDeckById(deck.getId());
        assertThat(refreshedDeck2.getCards()).hasSize(0);
    }
}
