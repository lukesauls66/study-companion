package com.lukesauls.studycompanion.studycompanion_backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.lukesauls.studycompanion.studycompanion_backend.dto.CardDto;
import com.lukesauls.studycompanion.studycompanion_backend.exception.card.CardNotFoundException;
import com.lukesauls.studycompanion.studycompanion_backend.exception.deck.DeckNotFoundException;
import com.lukesauls.studycompanion.studycompanion_backend.exception.card.InvalidCardCreationException;
import com.lukesauls.studycompanion.studycompanion_backend.exception.card.InvalidCardUpdateException;
import com.lukesauls.studycompanion.studycompanion_backend.exception.card.UnauthorizedCardAccessException;
import com.lukesauls.studycompanion.studycompanion_backend.exception.deck.UnauthorizedDeckAccessException;
import com.lukesauls.studycompanion.studycompanion_backend.model.CardCreationType;
import com.lukesauls.studycompanion.studycompanion_backend.model.postgres.Card;
import com.lukesauls.studycompanion.studycompanion_backend.model.postgres.Deck;
import com.lukesauls.studycompanion.studycompanion_backend.repository.postgres.CardRepository;

@Service
public class CardService {

    @Autowired 
    private CardRepository cardRepository;

    @Autowired 
    private DeckService deckService;
    
    /**
     * Create a new card for the specified deck.
     * Validates that the question and answer fields are not empty.
     * Maintains bidirectional relationship by adding card to deck's collection
     * 
     * @param cardDto the card creation data containing deckId, question, and answer
     * @param imageUrl the optional imageUrl to display the image on the card
     * @param creationType the creation type: MANUAL_UPLOAD or AI_PARSED
     * @param requestingUserId the UUID of the user making the request
     * @return the created card with generated ID and timestamps
     * @throws InvalidCardCreationException if question or answer is blank
     * @throws DeckNotFoundException if the specified deck does not exist
     * @throws UnauthorizedDeckAccessException if the requesting user is not the deck owner
     */
    @SuppressWarnings("null")
    public @NonNull Card createCard(@NonNull CardDto.Create cardDto, @NonNull CardCreationType creationType, @NonNull UUID requestingUserId) {
        Deck deck = deckService.getDeckById(cardDto.deckId());

        if (!deck.getUser().getId().equals(requestingUserId)) {
            throw new UnauthorizedDeckAccessException("You can only add cards to your own decks");
        }

        if (cardDto.question().trim().isEmpty()) {
            throw new InvalidCardCreationException("Question cannot be empty");
        }

        if (cardDto.answer().trim().isEmpty()) {
            throw new InvalidCardCreationException("Answer cannot be empty");
        }

        Card card;
        if (cardDto.imageUrl() == null) {
            card = new Card(deck, cardDto.question().trim(), cardDto.answer().trim(), creationType);
        } else {
            card = new Card(deck, cardDto.question().trim(), cardDto.answer().trim(), creationType, cardDto.imageUrl());
        }

        deck.addCard(card);

        return cardRepository.save(card);
    }

    /**
     * Retrieves a card by its unique identifier
     * 
     * @param id the UUID of the card to retrieve
     * @return the card with the specified ID
     * @throws CardNotFoundException if no card exists with the given ID
     */
    @SuppressWarnings("null")
    public @NonNull Card getCardById(@NonNull UUID id) {
        if (!cardRepository.existsById(id)) {
            throw new CardNotFoundException("Card with ID " + id + " not found");
        }

        return cardRepository.findById(id).get();
    }

    /**
     * Retrieves all cards in the system.
     * Currently unrestricted - should be limited to admin users in production.
     * 
     * @return a list of all cards in the system
     */
    //FIXME: Add requestUUID and only fetch if UUID belongs to an admin
    public List<Card> getAllCards() {
        return cardRepository.findAll();
    }

    /**
     * Retrieves all cards belonging to a specific deck
     * 
     * @param deckId the UUID of the deck whose cards to retrieve
     * @return a list of cards owned by the deck, empty if deck has no cards
     */
    public List<Card> getAllDeckCards(@NonNull UUID deckId) {
        return cardRepository.findByDeckId(deckId);
    }

    /**
     * Counts the total number of cards belonging to a specific deck
     * 
     * @param deckId the UUID of the deck whose card count to retrieve
     * @return the number of cards owned by the deck
     */
    public long getCountOfAllDeckCards(@NonNull UUID deckId) {
        return cardRepository.countByDeckId(deckId);
    }

    /**
     * Updates an existing card's question, answer, and/or imageUrl.
     * only the card/deck owner can perform this operation.
     * At least one field must be provided for update.
     * 
     * @param cardId the UUID of the card to update
     * @param cardDto the update data containing the new question, answer, and/or imageUrl
     * @param requestingUserId the UUID of the user making the request
     * @return the updated card
     * @throws CardNotFoundException if the card does not exist
     * @throws InvalidCardUpdateException if no valid fields are provided for update
     * @throws UnauthorizedCardAccessException if the requesting user is not the card/deck owner
     */
    public Card updateCard(@NonNull UUID cardId, @NonNull CardDto.Update cardDto, @NonNull UUID requestingUserId) {
        boolean isQuestionProvided = cardDto.question() != null && !cardDto.question().trim().isEmpty();
        boolean isAnswerProvided = cardDto.answer() != null && !cardDto.answer().trim().isEmpty();
        boolean isImageUrlProvided = cardDto.imageUrl() != null && !cardDto.imageUrl().trim().isEmpty();

        if (!isQuestionProvided && !isAnswerProvided && !isImageUrlProvided) {
            throw new InvalidCardUpdateException("At least one field must be provided");
        }

        Card existingCard = getCardById(cardId);

        if (!existingCard.getDeck().getUser().getId().equals(requestingUserId)) {
            throw new UnauthorizedCardAccessException("You can only update your own cards");
        }

        if (isQuestionProvided) {
            existingCard.setQuestion(cardDto.question().trim());
        }

        if (isAnswerProvided) {
            existingCard.setAnswer(cardDto.answer().trim());
        }

        if (isImageUrlProvided) {
            existingCard.setImageUrl(cardDto.imageUrl().trim());
        }
        return cardRepository.save(existingCard);
    }

    /**
     * Deletes a card by its unique identifier.
     * Only the card owner can perform this action.
     * Maintains bidirectional relationship by removing card from deck's collection.
     * 
     * @param cardId the UUID of the card to delete
     * @param requestingUserId the UUID of the user making the request
     * @throws CardNotFoundException if the card does not exist
     * @throws UnauthorizedCardAccessException if the requesting user is not the card owner
     */
    public void deleteCardById(@NonNull UUID cardId, @NonNull UUID requestingUserId) {
        Card card = getCardById(cardId);

        if (!card.getDeck().getUser().getId().equals(requestingUserId)) {
            throw new UnauthorizedCardAccessException("You can only delete your own cards");
        }

        Deck deck = card.getDeck();
        deck.removeCard(card);

        cardRepository.deleteById(cardId);
    }

    /**
     * Deletes all cards belonging to a specific deck.
     * This is typically used for administrative purposes or account deletion
     * Maintains bidirectional relationship by clearing deck's card collection
     * Currently unrestricted - should be limited to admin users in production
     * 
     * @param deckId the UUID of the deck whose cards to delete
     * @throws DeckNotFoundException if the specified deck does not exist
     */
    //FIXME: Add requestUUID and only delete if UUID belongs to an admin
    public void deleteAllDeckCards(@NonNull UUID deckId) {
        Deck deck = deckService.getDeckById(deckId);

        deck.getCards().clear();

        cardRepository.deleteByDeckId(deckId);
    }
}
