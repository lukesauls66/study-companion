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

import com.study_companion.backend.dto.CardDto;
import com.study_companion.backend.dto.DeckDto;
import com.study_companion.backend.dto.UserDto;
import com.study_companion.backend.exception.card.CardNotFoundException;
import com.study_companion.backend.exception.card.InvalidCardCreationException;
import com.study_companion.backend.exception.card.InvalidCardParameterException;
import com.study_companion.backend.exception.card.InvalidCardUpdateException;
import com.study_companion.backend.exception.card.UnauthorizedCardAccessException;
import com.study_companion.backend.exception.deck.UnauthorizedDeckAccessException;
import com.study_companion.backend.model.CardCreationType;

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
    @WithMockUser(roles = "ADMIN")
    void createCard_ValidInput_ReturnsCard() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");

        DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

        CardDto.CreateRequest cardCreateDto1 = new CardDto.CreateRequest(deck.deckId(), "Test question?", "Test answer",
                null);
        CardDto.CreateRequest cardCreateDto2 = new CardDto.CreateRequest(deck.deckId(), "Test question 2?",
                "Test answer 2",
                "testurl.jpg");

        cardService.createCard(cardCreateDto1, CardCreationType.MANUAL_UPLOAD, user.id());
        cardService.createCard(cardCreateDto2, CardCreationType.AI_PARSED, user.id());

        DeckDto.GetResponseWithCardsAndUploads deckWithCards = deckService
                .getDeckByIdWithCardsAndUploads(deck.deckId());

        List<CardDto.GetResponse> cards = deckWithCards.cards();
        assertThat(cards).hasSize(2);
        assertThat(cards).extracting(CardDto.GetResponse::question).containsExactlyInAnyOrder("Test question?",
                "Test question 2?");
        assertThat(cards).extracting(CardDto.GetResponse::answer).containsExactlyInAnyOrder("Test answer",
                "Test answer 2");
        assertThat(cards).extracting(CardDto.GetResponse::imageUrl).containsExactlyInAnyOrder(null, "testurl.jpg");
        assertThat(cards).extracting(CardDto.GetResponse::creationType).containsExactlyInAnyOrder(
                CardCreationType.AI_PARSED,
                CardCreationType.MANUAL_UPLOAD);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCard_InvalidUser_ThrowsUnauthorizedDeckAccessException() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");

        DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

        CardDto.CreateRequest cardCreateDto = new CardDto.CreateRequest(deck.deckId(), "Test question?", "Test answer",
                null);

        UUID unauthorizedUUID = UUID.randomUUID();

        UnauthorizedDeckAccessException exception = assertThrows(UnauthorizedDeckAccessException.class, () -> {
            cardService.createCard(cardCreateDto, CardCreationType.MANUAL_UPLOAD, unauthorizedUUID);
        });

        assertThat(exception.getMessage()).isEqualTo("You can only add cards to your own decks");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCard_BlankQuestion_ThrowsInvalidCardCreationException() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");

        DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

        CardDto.CreateRequest cardCreateDto = new CardDto.CreateRequest(deck.deckId(), " ", "Test answer", null);

        InvalidCardCreationException exception = assertThrows(InvalidCardCreationException.class, () -> {
            cardService.createCard(cardCreateDto, CardCreationType.MANUAL_UPLOAD, user.id());
        });

        assertThat(exception.getMessage()).isEqualTo("Question cannot be empty");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCard_BlankAnswer_ThrowsInvalidCardCreationException() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");

        DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

        CardDto.CreateRequest cardCreateDto = new CardDto.CreateRequest(deck.deckId(), "Test question?", " ", null);

        InvalidCardCreationException exception = assertThrows(InvalidCardCreationException.class, () -> {
            cardService.createCard(cardCreateDto, CardCreationType.MANUAL_UPLOAD, user.id());
        });

        assertThat(exception.getMessage()).isEqualTo("Answer cannot be empty");
    }

    @Test
    void createCard_NullCardDto_ThrowsInvalidCardParameterException() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        InvalidCardParameterException exception = assertThrows(InvalidCardParameterException.class, () -> {
            cardService.createCard(null, CardCreationType.MANUAL_UPLOAD, user.id());
        });

        assertThat(exception.getMessage()).isEqualTo("Card data transfer object cannot be null");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCard_NullCreationType_ThrowsInvalidCardParameterException() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");

        DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

        CardDto.CreateRequest cardCreateDto = new CardDto.CreateRequest(deck.deckId(), "Test question?", "Test answer",
                null);

        InvalidCardParameterException exception = assertThrows(InvalidCardParameterException.class, () -> {
            cardService.createCard(cardCreateDto, null, user.id());
        });

        assertThat(exception.getMessage()).isEqualTo("creationType cannot be null");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCard_NullRequestingUserId_ThrowsInvalidCardParameterException() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");

        DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

        CardDto.CreateRequest cardCreateDto = new CardDto.CreateRequest(deck.deckId(), "Test question?", "Test answer",
                null);

        InvalidCardParameterException exception = assertThrows(InvalidCardParameterException.class, () -> {
            cardService.createCard(cardCreateDto, CardCreationType.MANUAL_UPLOAD, null);
        });

        assertThat(exception.getMessage()).isEqualTo("Requesting userId cannot be null");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCardById_ValidInput_ReturnsCard() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");

        DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

        CardDto.CreateRequest cardCreateDto = new CardDto.CreateRequest(deck.deckId(), "Test question?", "Test answer",
                null);

        CardDto.GetResponse createdCard = cardService.createCard(cardCreateDto, CardCreationType.MANUAL_UPLOAD,
                user.id());
        CardDto.GetResponse card = cardService.getCardById(createdCard.id());

        assertThat(card.question()).isEqualTo("Test question?");
        assertThat(card.answer()).isEqualTo("Test answer");
        assertThat(card.imageUrl()).isEqualTo(null);
        assertThat(card.creationType()).isEqualTo(CardCreationType.MANUAL_UPLOAD);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCardById_NullId_ThrowsInvalidCardParameterException() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");

        DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

        CardDto.CreateRequest cardCreateDto = new CardDto.CreateRequest(deck.deckId(), "Test question?", "Test answer",
                null);

        cardService.createCard(cardCreateDto, CardCreationType.MANUAL_UPLOAD, user.id());

        InvalidCardParameterException exception = assertThrows(InvalidCardParameterException.class, () -> {
            cardService.getCardById(null);
        });

        assertThat(exception.getMessage()).isEqualTo("id cannot be null");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCardById_NonExistentId_ThrowsCardNotFoundException() {
        UUID nonExistentId = UUID.randomUUID();

        CardNotFoundException exception = assertThrows(CardNotFoundException.class, () -> {
            cardService.getCardById(nonExistentId);
        });

        assertThat(exception.getMessage()).isEqualTo("Card with ID " + nonExistentId + " not found");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCards_ValidInput_ReturnsCards() {
        UserDto.CreateRequest userCreateDto1 = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");
        UserDto.CreateRequest userCreateDto2 = new UserDto.CreateRequest("test2@email.com", "Jane Smith", "jane123",
                "password2");

        UserDto.GetResponse user1 = userService.createUser(userCreateDto1);
        UserDto.GetResponse user2 = userService.createUser(userCreateDto2);

        DeckDto.CreateRequest deckCreateDto1 = new DeckDto.CreateRequest(user1.id(), "Test Deck", "Testing");
        DeckDto.CreateRequest deckCreateDto2 = new DeckDto.CreateRequest(user2.id(), "Test Deck 2", "Testing 2");

        DeckDto.GetResponse deck1 = deckService.createDeck(deckCreateDto1);
        DeckDto.GetResponse deck2 = deckService.createDeck(deckCreateDto2);

        CardDto.CreateRequest cardCreateDto1 = new CardDto.CreateRequest(deck1.deckId(), "Test question?",
                "Test answer", null);
        CardDto.CreateRequest cardCreateDto2 = new CardDto.CreateRequest(deck1.deckId(), "Test question 2?",
                "Test answer 2", null);
        CardDto.CreateRequest cardCreateDto3 = new CardDto.CreateRequest(deck2.deckId(), "Test question 3?",
                "Test answer 3",
                "testurl.jpg");

        cardService.createCard(cardCreateDto1, CardCreationType.MANUAL_UPLOAD, user1.id());
        cardService.createCard(cardCreateDto2, CardCreationType.MANUAL_UPLOAD, user1.id());
        cardService.createCard(cardCreateDto3, CardCreationType.MANUAL_UPLOAD, user2.id());

        List<CardDto.GetResponse> cards = cardService.getAllCards();

        assertThat(cards).hasSize(3);
        assertThat(cards).extracting(CardDto.GetResponse::question).containsExactlyInAnyOrder("Test question?",
                "Test question 2?",
                "Test question 3?");
        assertThat(cards).extracting(CardDto.GetResponse::answer).containsExactlyInAnyOrder("Test answer",
                "Test answer 2",
                "Test answer 3");
        assertThat(cards).extracting(CardDto.GetResponse::imageUrl).containsExactlyInAnyOrder("testurl.jpg", null,
                null);
        assertThat(cards).extracting(CardDto.GetResponse::creationType).containsExactlyInAnyOrder(
                CardCreationType.MANUAL_UPLOAD,
                CardCreationType.MANUAL_UPLOAD, CardCreationType.MANUAL_UPLOAD);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllCards_NonAdmin_ThrowsUnauthorizedCardAccessException() {
        UserDto.CreateRequest userCreateDto1 = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");

        UserDto.GetResponse user1 = userService.createUser(userCreateDto1);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user1.id().toString(), null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        try {
            DeckDto.CreateRequest deckCreateDto1 = new DeckDto.CreateRequest(user1.id(), "Test Deck", "Testing");

            DeckDto.GetResponse deck1 = deckService.createDeck(deckCreateDto1);

            CardDto.CreateRequest cardCreateDto1 = new CardDto.CreateRequest(deck1.deckId(), "Test question?",
                    "Test answer", null);
            CardDto.CreateRequest cardCreateDto2 = new CardDto.CreateRequest(deck1.deckId(), "Test question 2?",
                    "Test answer 2", null);

            cardService.createCard(cardCreateDto1, CardCreationType.MANUAL_UPLOAD, user1.id());
            cardService.createCard(cardCreateDto2, CardCreationType.MANUAL_UPLOAD, user1.id());

            UnauthorizedCardAccessException exception = assertThrows(UnauthorizedCardAccessException.class, () -> {
                cardService.getAllCards();
            });

            assertThat(exception.getMessage()).isEqualTo("Unauthorized user access");
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllDeckCards_ValidInput_ReturnsCards() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");

        DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

        CardDto.CreateRequest cardCreateDto1 = new CardDto.CreateRequest(deck.deckId(), "Test question?", "Test answer",
                null);
        CardDto.CreateRequest cardCreateDto2 = new CardDto.CreateRequest(deck.deckId(), "Test question 2?",
                "Test answer 2", null);
        CardDto.CreateRequest cardCreateDto3 = new CardDto.CreateRequest(deck.deckId(), "Test question 3?",
                "Test answer 3",
                "testurl.jpg");

        cardService.createCard(cardCreateDto1, CardCreationType.MANUAL_UPLOAD, user.id());
        cardService.createCard(cardCreateDto2, CardCreationType.MANUAL_UPLOAD, user.id());
        cardService.createCard(cardCreateDto3, CardCreationType.MANUAL_UPLOAD, user.id());

        List<CardDto.GetResponse> cards = cardService.getAllDeckCards(deck.deckId());

        assertThat(cards).hasSize(3);
        assertThat(cards).extracting(CardDto.GetResponse::question).containsExactlyInAnyOrder("Test question?",
                "Test question 2?",
                "Test question 3?");
        assertThat(cards).extracting(CardDto.GetResponse::answer).containsExactlyInAnyOrder("Test answer",
                "Test answer 2",
                "Test answer 3");
        assertThat(cards).extracting(CardDto.GetResponse::imageUrl).containsExactlyInAnyOrder("testurl.jpg", null,
                null);
        assertThat(cards).extracting(CardDto.GetResponse::creationType).containsExactlyInAnyOrder(
                CardCreationType.MANUAL_UPLOAD,
                CardCreationType.MANUAL_UPLOAD, CardCreationType.MANUAL_UPLOAD);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllDeckCards_NullDeckId_ThrowsInvalidCardParameterException() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");

        DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

        CardDto.CreateRequest cardCreateDto1 = new CardDto.CreateRequest(deck.deckId(), "Test question?", "Test answer",
                null);
        CardDto.CreateRequest cardCreateDto2 = new CardDto.CreateRequest(deck.deckId(), "Test question 2?",
                "Test answer 2", null);
        CardDto.CreateRequest cardCreateDto3 = new CardDto.CreateRequest(deck.deckId(), "Test question 3?",
                "Test answer 3",
                "testurl.jpg");

        cardService.createCard(cardCreateDto1, CardCreationType.MANUAL_UPLOAD, user.id());
        cardService.createCard(cardCreateDto2, CardCreationType.MANUAL_UPLOAD, user.id());
        cardService.createCard(cardCreateDto3, CardCreationType.MANUAL_UPLOAD, user.id());

        InvalidCardParameterException exception = assertThrows(InvalidCardParameterException.class, () -> {
            cardService.getAllDeckCards(null);
        });

        assertThat(exception.getMessage()).isEqualTo("deckId cannot be null");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCountOfAllDeckCards_ValidInput_ReturnsCardCount() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");

        DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

        CardDto.CreateRequest cardCreateDto1 = new CardDto.CreateRequest(deck.deckId(), "Test question?", "Test answer",
                null);
        CardDto.CreateRequest cardCreateDto2 = new CardDto.CreateRequest(deck.deckId(), "Test question 2?",
                "Test answer 2", null);
        CardDto.CreateRequest cardCreateDto3 = new CardDto.CreateRequest(deck.deckId(), "Test question 3?",
                "Test answer 3",
                "testurl.jpg");

        cardService.createCard(cardCreateDto1, CardCreationType.MANUAL_UPLOAD, user.id());
        cardService.createCard(cardCreateDto2, CardCreationType.MANUAL_UPLOAD, user.id());
        cardService.createCard(cardCreateDto3, CardCreationType.MANUAL_UPLOAD, user.id());

        long cardCount = cardService.getCountOfAllDeckCards(deck.deckId());

        assertThat(cardCount).isEqualTo(3);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCountOfAllDeckCards_NullDeckId_ThrowsInvalidCardParameterException() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");

        DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

        CardDto.CreateRequest cardCreateDto1 = new CardDto.CreateRequest(deck.deckId(), "Test question?", "Test answer",
                null);
        CardDto.CreateRequest cardCreateDto2 = new CardDto.CreateRequest(deck.deckId(), "Test question 2?",
                "Test answer 2", null);
        CardDto.CreateRequest cardCreateDto3 = new CardDto.CreateRequest(deck.deckId(), "Test question 3?",
                "Test answer 3",
                "testurl.jpg");

        cardService.createCard(cardCreateDto1, CardCreationType.MANUAL_UPLOAD, user.id());
        cardService.createCard(cardCreateDto2, CardCreationType.MANUAL_UPLOAD, user.id());
        cardService.createCard(cardCreateDto3, CardCreationType.MANUAL_UPLOAD, user.id());

        InvalidCardParameterException exception = assertThrows(InvalidCardParameterException.class, () -> {
            cardService.getCountOfAllDeckCards(null);
        });

        assertThat(exception.getMessage()).isEqualTo("deckId cannot be null");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCard_ValidInput_ReturnsUpdatedCard() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");

        DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

        CardDto.CreateRequest cardCreateDto = new CardDto.CreateRequest(deck.deckId(), "Test question?", "Test answer",
                null);

        CardDto.GetResponse card = cardService.createCard(cardCreateDto, CardCreationType.MANUAL_UPLOAD, user.id());

        CardDto.UpdateRequest cardUpdateDto = new CardDto.UpdateRequest("Updated question?", "Updated answer",
                "updated-image.jpg");

        CardDto.GetResponse updatedCard = cardService.updateCard(card.id(), cardUpdateDto, user.id());

        assertThat(updatedCard.id()).isEqualTo(card.id());
        assertThat(updatedCard.deckId()).isEqualTo(deck.deckId());
        assertThat(updatedCard.question()).isEqualTo("Updated question?");
        assertThat(updatedCard.answer()).isEqualTo("Updated answer");
        assertThat(updatedCard.imageUrl()).isEqualTo("updated-image.jpg");
        assertThat(updatedCard.creationType()).isEqualTo(CardCreationType.MANUAL_UPLOAD);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCard_NullCardId_ThrowsInvalidCardParameterException() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");

        DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

        CardDto.CreateRequest cardCreateDto = new CardDto.CreateRequest(deck.deckId(), "Test question?", "Test answer",
                null);

        cardService.createCard(cardCreateDto, CardCreationType.MANUAL_UPLOAD, user.id());

        CardDto.UpdateRequest cardUpdateDto = new CardDto.UpdateRequest("Updated question?", "Updated answer",
                "updated-image.jpg");

        InvalidCardParameterException exception = assertThrows(InvalidCardParameterException.class, () -> {
            cardService.updateCard(null, cardUpdateDto, user.id());
        });

        assertThat(exception.getMessage()).isEqualTo("cardId cannot be null");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCard_NullCardDto_ThrowsInvalidCardParameterException() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");

        DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

        CardDto.CreateRequest cardCreateDto = new CardDto.CreateRequest(deck.deckId(), "Test question?", "Test answer",
                null);

        CardDto.GetResponse card = cardService.createCard(cardCreateDto, CardCreationType.MANUAL_UPLOAD, user.id());

        InvalidCardParameterException exception = assertThrows(InvalidCardParameterException.class, () -> {
            cardService.updateCard(card.id(), null, user.id());
        });

        assertThat(exception.getMessage()).isEqualTo("Card data transfer object cannot be null");
    }

    @Test
    void updateCard_NullRequestingUserId_ThrowsInvalidCardParameterException() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user.id().toString(), null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        try {
            DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");

            DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

            CardDto.CreateRequest cardCreateDto = new CardDto.CreateRequest(deck.deckId(), "Test question?",
                    "Test answer", null);

            CardDto.GetResponse card = cardService.createCard(cardCreateDto, CardCreationType.MANUAL_UPLOAD, user.id());

            CardDto.UpdateRequest cardUpdateDto = new CardDto.UpdateRequest("Updated question?", "Updated answer",
                    "updated-image.jpg");

            InvalidCardParameterException exception = assertThrows(InvalidCardParameterException.class, () -> {
                cardService.updateCard(card.id(), cardUpdateDto, null);
            });

            assertThat(exception.getMessage()).isEqualTo("Requesting userId cannot be null");
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void updateCard_InvalidInput_ThrowsInvalidCardUpdateException() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user.id().toString(), null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        try {
            DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");

            DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

            CardDto.CreateRequest cardCreateDto = new CardDto.CreateRequest(deck.deckId(), "Test question?",
                    "Test answer", null);

            CardDto.GetResponse card = cardService.createCard(cardCreateDto, CardCreationType.MANUAL_UPLOAD, user.id());

            CardDto.UpdateRequest cardUpdateDto = new CardDto.UpdateRequest("", "", "");

            InvalidCardUpdateException exception = assertThrows(InvalidCardUpdateException.class, () -> {
                cardService.updateCard(card.id(), cardUpdateDto, user.id());
            });

            assertThat(exception.getMessage()).isEqualTo("At least one field must be provided");
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void updateCard_UnauthorizedUser_ThrowsUnauthorizedCardAccessException() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user.id().toString(), null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        try {
            DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");

            DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

            CardDto.CreateRequest cardCreateDto = new CardDto.CreateRequest(deck.deckId(), "Test question?",
                    "Test answer", null);

            CardDto.GetResponse card = cardService.createCard(cardCreateDto, CardCreationType.MANUAL_UPLOAD, user.id());

            CardDto.UpdateRequest cardUpdateDto = new CardDto.UpdateRequest("Updated question?", "Updated answer",
                    "updated-image.jpg");

            UUID unauthorizedUUID = UUID.randomUUID();

            UnauthorizedCardAccessException exception = assertThrows(UnauthorizedCardAccessException.class, () -> {
                cardService.updateCard(card.id(), cardUpdateDto, unauthorizedUUID);
            });

            assertThat(exception.getMessage()).isEqualTo("You can only update your own cards");
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCardById_ValidInput() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");

        DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

        CardDto.CreateRequest cardCreateDto = new CardDto.CreateRequest(deck.deckId(), "Test question?", "Test answer",
                null);

        CardDto.GetResponse card = cardService.createCard(cardCreateDto, CardCreationType.MANUAL_UPLOAD, user.id());

        DeckDto.GetResponseWithCardsAndUploads refreshedDeck1 = deckService
                .getDeckByIdWithCardsAndUploads(deck.deckId());
        assertThat(refreshedDeck1.cards()).hasSize(1);

        cardService.deleteCardById(card.id(), user.id());

        DeckDto.GetResponseWithCardsAndUploads refreshedDeck2 = deckService
                .getDeckByIdWithCardsAndUploads(deck.deckId());
        assertThat(refreshedDeck2.cards()).hasSize(0);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCardById_NullCardId_ThrowsInvalidCardParameterException() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");

        DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

        CardDto.CreateRequest cardCreateDto = new CardDto.CreateRequest(deck.deckId(), "Test question?", "Test answer",
                null);

        cardService.createCard(cardCreateDto, CardCreationType.MANUAL_UPLOAD, user.id());

        InvalidCardParameterException exception = assertThrows(InvalidCardParameterException.class, () -> {
            cardService.deleteCardById(null, user.id());
        });

        assertThat(exception.getMessage()).isEqualTo("cardId cannot be null");
    }

    @Test
    void deleteCardById_NullRequestingUserId_ThrowsInvalidCardParameterException() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user.id().toString(), null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        try {
            DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");

            DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

            CardDto.CreateRequest cardCreateDto = new CardDto.CreateRequest(deck.deckId(), "Test question?",
                    "Test answer", null);

            CardDto.GetResponse card = cardService.createCard(cardCreateDto, CardCreationType.MANUAL_UPLOAD, user.id());

            InvalidCardParameterException exception = assertThrows(InvalidCardParameterException.class, () -> {
                cardService.deleteCardById(card.id(), null);
            });

            assertThat(exception.getMessage()).isEqualTo("Requesting userId cannot be null");
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCardById_UnauthorizedUser_ThrowsUnauthorizedCardAccessException() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");

        DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

        CardDto.CreateRequest cardCreateDto = new CardDto.CreateRequest(deck.deckId(), "Test question?", "Test answer",
                null);

        CardDto.GetResponse card = cardService.createCard(cardCreateDto, CardCreationType.MANUAL_UPLOAD, user.id());

        UUID unauthorizedUUID = UUID.randomUUID();

        UnauthorizedCardAccessException exception = assertThrows(UnauthorizedCardAccessException.class, () -> {
            cardService.deleteCardById(card.id(), unauthorizedUUID);
        });

        assertThat(exception.getMessage()).isEqualTo("You can only delete your own cards");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteAllDeckCards_ValidInput() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");

        DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

        CardDto.CreateRequest cardCreateDto1 = new CardDto.CreateRequest(deck.deckId(), "Test question?", "Test answer",
                null);
        CardDto.CreateRequest cardCreateDto2 = new CardDto.CreateRequest(deck.deckId(), "Test question 2?",
                "Test answer 2", null);
        CardDto.CreateRequest cardCreateDto3 = new CardDto.CreateRequest(deck.deckId(), "Test question 3?",
                "Test answer 3",
                "testurl.jpg");

        cardService.createCard(cardCreateDto1, CardCreationType.MANUAL_UPLOAD, user.id());
        cardService.createCard(cardCreateDto2, CardCreationType.MANUAL_UPLOAD, user.id());
        cardService.createCard(cardCreateDto3, CardCreationType.MANUAL_UPLOAD, user.id());

        DeckDto.GetResponseWithCardsAndUploads refreshedDeck1 = deckService
                .getDeckByIdWithCardsAndUploads(deck.deckId());
        assertThat(refreshedDeck1.cards()).hasSize(3);

        cardService.deleteAllDeckCards(deck.deckId());

        DeckDto.GetResponseWithCardsAndUploads refreshedDeck2 = deckService
                .getDeckByIdWithCardsAndUploads(deck.deckId());
        assertThat(refreshedDeck2.cards()).hasSize(0);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteAllDeckCards_NullDeckId_ThrowsInvalidCardParameterException() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");

        DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

        CardDto.CreateRequest cardCreateDto1 = new CardDto.CreateRequest(deck.deckId(), "Test question?", "Test answer",
                null);
        CardDto.CreateRequest cardCreateDto2 = new CardDto.CreateRequest(deck.deckId(), "Test question 2?",
                "Test answer 2", null);
        CardDto.CreateRequest cardCreateDto3 = new CardDto.CreateRequest(deck.deckId(), "Test question 3?",
                "Test answer 3",
                "testurl.jpg");

        cardService.createCard(cardCreateDto1, CardCreationType.MANUAL_UPLOAD, user.id());
        cardService.createCard(cardCreateDto2, CardCreationType.MANUAL_UPLOAD, user.id());
        cardService.createCard(cardCreateDto3, CardCreationType.MANUAL_UPLOAD, user.id());

        InvalidCardParameterException exception = assertThrows(InvalidCardParameterException.class, () -> {
            cardService.deleteAllDeckCards(null);
        });

        assertThat(exception.getMessage()).isEqualTo("deckId cannot be null");
    }

    @Test
    void deleteAllDeckCards_NonAdmin_ThrowsUnauthorizedCardAccessException() {
        UserDto.CreateRequest userCreateDto = new UserDto.CreateRequest("test@email.com", "John Smith", "john123",
                "password");

        UserDto.GetResponse user = userService.createUser(userCreateDto);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user.id().toString(), null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        try {
            DeckDto.CreateRequest deckCreateDto = new DeckDto.CreateRequest(user.id(), "Test Deck", "Testing");

            DeckDto.GetResponse deck = deckService.createDeck(deckCreateDto);

            CardDto.CreateRequest cardCreateDto1 = new CardDto.CreateRequest(deck.deckId(), "Test question?",
                    "Test answer", null);
            CardDto.CreateRequest cardCreateDto2 = new CardDto.CreateRequest(deck.deckId(), "Test question 2?",
                    "Test answer 2", null);
            CardDto.CreateRequest cardCreateDto3 = new CardDto.CreateRequest(deck.deckId(), "Test question 3?",
                    "Test answer 3",
                    "testurl.jpg");

            cardService.createCard(cardCreateDto1, CardCreationType.MANUAL_UPLOAD, user.id());
            cardService.createCard(cardCreateDto2, CardCreationType.MANUAL_UPLOAD, user.id());
            cardService.createCard(cardCreateDto3, CardCreationType.MANUAL_UPLOAD, user.id());

            DeckDto.GetResponseWithCardsAndUploads refreshedDeck1 = deckService
                    .getDeckByIdWithCardsAndUploads(deck.deckId());
            assertThat(refreshedDeck1.cards()).hasSize(3);

            UnauthorizedCardAccessException exception = assertThrows(UnauthorizedCardAccessException.class, () -> {
                cardService.deleteAllDeckCards(deck.deckId());
            });

            assertThat(exception.getMessage()).isEqualTo("Unauthorized user access");
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
