package com.lukesauls.studycompanion.studycompanion_backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import com.lukesauls.studycompanion.studycompanion_backend.dto.CardDto;
import com.lukesauls.studycompanion.studycompanion_backend.dto.DeckDto;
import com.lukesauls.studycompanion.studycompanion_backend.dto.UserDto;
import com.lukesauls.studycompanion.studycompanion_backend.model.CardCreationType;
import com.lukesauls.studycompanion.studycompanion_backend.model.postgres.Card;
import com.lukesauls.studycompanion.studycompanion_backend.model.postgres.Deck;
import com.lukesauls.studycompanion.studycompanion_backend.model.postgres.User;

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
    @SuppressWarnings("null")
    void createCard_ValidInput_ReturnsCard() {
        UserDto.Create userCreateDto = new UserDto.Create("test@email.com", "John Smith", "john123", "password");

        User user = userService.createUser(userCreateDto);

        DeckDto.Create deckCreateDto = new DeckDto.Create(user.getId(), "Test Deck", "Testing");

        Deck deck = deckService.createDeck(deckCreateDto);

        CardDto.Create cardCreateDto1 = new CardDto.Create(deck.getId(), "Test question?", "Test answer", null);
        CardDto.Create cardCreateDto2 = new CardDto.Create(deck.getId(), "Test question 2?", "Test answer 2", "testurl.jpg");

        Card card1 = cardService.createCard(cardCreateDto1, CardCreationType.MANUAL_UPLOAD, user.getId());
        Card card2 = cardService.createCard(cardCreateDto2, CardCreationType.MANUAL_UPLOAD, user.getId());

        List<Card> cards = deck.getCards();
        assertThat(card1.getDeck().getUser().getId()).isEqualTo(user.getId());
        assertThat(card2.getDeck().getUser().getId()).isEqualTo(user.getId());
        assertThat(cards).hasSize(2);
        assertThat(cards).extracting(Card::getQuestion).containsExactlyInAnyOrder("Test question?", "Test question 2?");
        assertThat(cards).extracting(Card::getAnswer).containsExactlyInAnyOrder("Test answer", "Test answer 2");
        assertThat(cards).extracting(Card::getImageUrl).containsExactlyInAnyOrder(null, "testurl.jpg");
    }
}
