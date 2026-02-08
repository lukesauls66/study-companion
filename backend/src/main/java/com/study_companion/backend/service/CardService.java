package com.study_companion.backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.study_companion.backend.dto.CardDto;
import com.study_companion.backend.exception.card.CardException;
import com.study_companion.backend.exception.card.CardNotFoundException;
import com.study_companion.backend.exception.card.CardOperationException;
import com.study_companion.backend.exception.deck.DeckException;
import com.study_companion.backend.exception.deck.DeckNotFoundException;
import com.study_companion.backend.exception.deck.DeckOperationException;
import com.study_companion.backend.exception.card.InvalidCardCreationException;
import com.study_companion.backend.exception.card.InvalidCardParameterException;
import com.study_companion.backend.exception.card.InvalidCardUpdateException;
import com.study_companion.backend.exception.card.UnauthorizedCardAccessException;
import com.study_companion.backend.exception.deck.UnauthorizedDeckAccessException;
import com.study_companion.backend.model.CardCreationType;
import com.study_companion.backend.model.postgres.Card;
import com.study_companion.backend.model.postgres.Deck;
import com.study_companion.backend.repository.postgres.CardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory; 

@Service
public class CardService {

    private static final Logger logger = LoggerFactory.getLogger(CardService.class);

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private DeckService deckService;

    /**
     * Create a new card for the specified deck.
     * Validates that the question and answer fields are not empty.
     * Maintains bidirectional relationship by adding card to deck's collection
     * 
     * @param cardDto          the card creation data containing deckId, question,
     *                         and answer
     * @param imageUrl         the optional imageUrl to display the image on the
     *                         card
     * @param creationType     the creation type: MANUAL_UPLOAD or AI_PARSED
     * @param requestingUserId the UUID of the user making the request
     * @return the created card with generated ID and timestamps
     * @throws InvalidCardParameterException   if any nonnull arg is null
     * @throws InvalidDeckParameterException   if the deckId is invalid
     * @throws InvalidCardCreationException    if question or answer is blank
     * @throws DeckNotFoundException           if the specified deck does not exist
     * @throws UnauthorizedDeckAccessException if the requesting user is not the
     *                                         deck owner
     * @throws DeckOperationException          if deck operations fail
     * @throws CardOperationException          if server error occurs
     */
    public Card createCard(CardDto.Create cardDto, CardCreationType creationType,
            UUID requestingUserId) {
        if (cardDto == null) {
            throw new InvalidCardParameterException("Card data transfer object cannot be null");
        }

        if (creationType == null) {
            throw new InvalidCardParameterException("creationType cannot be null");
        }

        if (requestingUserId == null) {
            throw new InvalidCardParameterException("Requesting userId cannot be null");
        }

        if (cardDto.question().trim().isEmpty()) {
            throw new InvalidCardCreationException("Question cannot be empty");
        }

        if (cardDto.answer().trim().isEmpty()) {
            throw new InvalidCardCreationException("Answer cannot be empty");
        }

        try {
            Deck deck = deckService.getDeckById(cardDto.deckId());

            logger.debug("Verifying that the requesting user can add cards to this deck: {}", deck.getTitle());
            if (!deck.getUser().getId().equals(requestingUserId)) {
                throw new UnauthorizedDeckAccessException("You can only add cards to your own decks");
            }

            Card card;
            if (cardDto.imageUrl() == null) {
                logger.debug("Creating card without image");
                card = new Card(deck, cardDto.question().trim(), cardDto.answer().trim(), creationType);
            } else {
                logger.debug("Creating card with image");
                card = new Card(deck, cardDto.question().trim(), cardDto.answer().trim(), creationType,
                        cardDto.imageUrl());
            }

            logger.debug("Adding card to parent deck");
            deck.addCard(card);

            Card newCard = cardRepository.save(card);
            logger.info("Successfully created new card and added to parent deck");
            return newCard;
        } catch (DeckException e) {
            logger.error("Card creation failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to create card: {}", e.getMessage());
            throw new CardOperationException("Failed to create card", e);
        }
    }

    /**
     * Retrieves a card by its unique identifier
     * 
     * @param id the UUID of the card to retrieve
     * @return the card with the specified ID
     * @throws InvalidCardParameterException if any nonnull arg is null
     * @throws CardNotFoundException         if no card exists with the given ID
     * @throws CardOperationException        if server error occurs
     */
    public Card getCardById(UUID id) {
        if (id == null) {
            throw new InvalidCardParameterException("id cannot be null");
        }

        try {
            logger.debug("Checking if card exists");
            if (!cardRepository.existsById(id)) {
                throw new CardNotFoundException("Card with ID " + id + " not found");
            }

            Card card = cardRepository.findById(id).get();
            logger.info("Successfully found card by provided id");
            return card;
        } catch (CardNotFoundException e) {
            logger.error("Card does not exist with provided id: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to fetch card by provided id: {}", e.getMessage());
            throw new CardOperationException("Failed to fetch card by provided id", e);
        }
    }

    /**
     * Retrieves all cards in the system.
     * Currently unrestricted - should be limited to admin users in production.
     * 
     * @return a list of all cards in the system
     * @throws CardOperationException if server error occurs
     */
    // FIXME: Add requestUUID and only fetch if UUID belongs to an admin
    public List<Card> getAllCards() {
        try {
            logger.debug("Searching for all cards");
            List<Card> cards = cardRepository.findAll();
            logger.info("Successfully fetched all cards");
            return cards;
        } catch (Exception e) {
            logger.error("Failed to fetch all cards: {}", e.getMessage());
            throw new CardOperationException("Failed to fetch all cards", e);
        }
    }

    /**
     * Retrieves all cards belonging to a specific deck
     * 
     * @param deckId the UUID of the deck whose cards to retrieve
     * @return a list of cards owned by the deck, empty if deck has no cards
     * @throws InvalidCardParameterException if any nonnull arg is null
     * @throws CardOperationException        if server error occurs
     */
    public List<Card> getAllDeckCards(UUID deckId) {
        if (deckId == null) {
            throw new InvalidCardParameterException("deckId cannot be null");
        }

        try {
            logger.debug("Fetching all cards belonging to the provided deckId");
            List<Card> cards = cardRepository.findByDeckId(deckId);
            logger.info("Successfully fetched all cards belonging to the provided deckId");
            return cards;
        } catch (Exception e) {
            logger.error("Failed to fetch all cards belonging to the provided deckId: {}", e.getMessage());
            throw new CardOperationException("Failed to fetch all cards belonging to the provided deckId", e);
        }
    }

    /**
     * Counts the total number of cards belonging to a specific deck
     * 
     * @param deckId the UUID of the deck whose card count to retrieve
     * @return the number of cards owned by the deck
     * @throws InvalidCardParameterException if any nonnull arg is null
     * @throws CardOperationException        if server error occurs
     */
    public long getCountOfAllDeckCards(UUID deckId) {
        if (deckId == null) {
            throw new InvalidCardParameterException("deckId cannot be null");
        }

        try {
            logger.debug("Calculating number of cards belonging to the provided deckId");
            long cardCount = cardRepository.countByDeckId(deckId);
            logger.info("Successfully calculated {} cards belonging to the provided deckId", cardCount);
            return cardCount;
        } catch (Exception e) {
            logger.error("Failed to calculate number of cards belonging to the provided deckId: {}", e.getMessage());
            throw new CardOperationException("Failed to calculate number of cards belonging to the provided deckId", e);
        }
    }

    /**
     * Updates an existing card's question, answer, and/or imageUrl.
     * only the card/deck owner can perform this operation.
     * At least one field must be provided for update.
     * 
     * @param cardId           the UUID of the card to update
     * @param cardDto          the update data containing the new question, answer,
     *                         and/or imageUrl
     * @param requestingUserId the UUID of the user making the request
     * @return the updated card
     * @throws InvalidCardParameterException   if any nonnull arg is null
     * @throws CardNotFoundException           if the card does not exist
     * @throws InvalidCardUpdateException      if no valid fields are provided for
     *                                         update
     * @throws UnauthorizedCardAccessException if the requesting user is not the
     *                                         card/deck owner
     * @throws CardOperationException          if server error occurs
     */
    public Card updateCard(UUID cardId, CardDto.Update cardDto, UUID requestingUserId) {
        if (cardId == null) {
            throw new InvalidCardParameterException("cardId cannot be null");
        }

        if (cardDto == null) {
            throw new InvalidCardParameterException("Card data transfer object cannot be null");
        }

        if (requestingUserId == null) {
            throw new InvalidCardParameterException("Requesting userId cannot be null");
        }

        boolean isQuestionProvided = cardDto.question() != null && !cardDto.question().trim().isEmpty();
        boolean isAnswerProvided = cardDto.answer() != null && !cardDto.answer().trim().isEmpty();
        boolean isImageUrlProvided = cardDto.imageUrl() != null && !cardDto.imageUrl().trim().isEmpty();

        if (!isQuestionProvided && !isAnswerProvided && !isImageUrlProvided) {
            throw new InvalidCardUpdateException("At least one field must be provided");
        }

        try {
            Card existingCard = getCardById(cardId);

            logger.debug("Verifying request user is authorized to update this card");
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

            logger.debug("Updating card");
            Card updatedCard = cardRepository.save(existingCard);
            logger.info("Successfully updated card");
            return updatedCard;
        } catch (CardException e) {
            logger.error("Card update failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to update card: {}", e.getMessage());
            throw new CardOperationException("Failed to update card", e);
        }
    }

    /**
     * Deletes a card by its unique identifier.
     * Only the card owner can perform this action.
     * Maintains bidirectional relationship by removing card from deck's collection.
     * 
     * @param cardId           the UUID of the card to delete
     * @param requestingUserId the UUID of the user making the request
     * @throws InvalidCardParameterException   if any nonnull arg is null
     * @throws CardNotFoundException           if the card does not exist
     * @throws UnauthorizedCardAccessException if the requesting user is not the
     *                                         card owner
     * @throws CardOperationException          if server error occurs
     */
    public void deleteCardById(UUID cardId, UUID requestingUserId) {
        if (cardId == null) {
            throw new InvalidCardParameterException("cardId cannot be null");
        }

        if (requestingUserId == null) {
            throw new InvalidCardParameterException("Requesting userId cannot be null");
        }

        try {
            Card card = getCardById(cardId);

            logger.debug("Verifying request user is authorized to delete this card");
            if (!card.getDeck().getUser().getId().equals(requestingUserId)) {
                throw new UnauthorizedCardAccessException("You can only delete your own cards");
            }

            logger.debug("Fetching deck that owns this card");
            Deck deck = card.getDeck();
            logger.debug("Disconnecting the bidirectional connection between this card and its parent deck");
            deck.removeCard(card);

            logger.debug("Removed connection, deleting card now");
            cardRepository.deleteById(cardId);
            logger.info("Successfully deleted card");
        } catch (CardException e) {
            logger.error("Card deletion failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to delete card by the provided id: {}", e.getMessage());
            throw new CardOperationException("Failed to delete card by the provided id", e);
        }
    }

    /**
     * Deletes all cards belonging to a specific deck.
     * This is typically used for administrative purposes or account deletion
     * Maintains bidirectional relationship by clearing deck's card collection
     * Currently unrestricted - should be limited to admin users in production
     * 
     * @param deckId the UUID of the deck whose cards to delete
     * @throws InvalidCardParameterException if any nonnull arg is null
     * @throws InvalidDeckParameterException if the deckId is invalid
     * @throws DeckNotFoundException         if the specified deck does not exist
     * @throws DeckOperationException        if deck operations fail
     * @throws CardOperationException        if server error occurs
     */
    // FIXME: Add requestUUID and only delete if UUID belongs to an admin
    public void deleteAllDeckCards(UUID deckId) {
        if (deckId == null) {
            throw new InvalidCardParameterException("deckId cannot be null");
        }

        try {
            Deck deck = deckService.getDeckById(deckId);

            logger.debug("Clearing deck's cards");
            deck.getCards().clear();

            logger.debug("Deleting all cards belonging to the associated deck");
            cardRepository.deleteByDeckId(deckId);
            logger.info("Successfully deleted all cards from provided deck");
        } catch (DeckException e) {
            logger.error("Card deletions failed due to deck issue: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to delete all cards from the provided deck: {}", e.getMessage());
            throw new CardOperationException("Failed to delete all cards from the provided deck", e);
        }
    }
}
