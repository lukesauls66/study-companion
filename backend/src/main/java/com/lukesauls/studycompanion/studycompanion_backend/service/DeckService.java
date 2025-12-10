package com.lukesauls.studycompanion.studycompanion_backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.lukesauls.studycompanion.studycompanion_backend.dto.DeckDto;
import com.lukesauls.studycompanion.studycompanion_backend.exception.deck.DeckException;
import com.lukesauls.studycompanion.studycompanion_backend.exception.deck.DeckNotFoundException;
import com.lukesauls.studycompanion.studycompanion_backend.exception.deck.DeckOperationException;
import com.lukesauls.studycompanion.studycompanion_backend.exception.deck.InvalidDeckCreationException;
import com.lukesauls.studycompanion.studycompanion_backend.exception.deck.InvalidDeckParameterException;
import com.lukesauls.studycompanion.studycompanion_backend.exception.deck.InvalidDeckUpdateException;
import com.lukesauls.studycompanion.studycompanion_backend.exception.deck.UnauthorizedDeckAccessException;
import com.lukesauls.studycompanion.studycompanion_backend.exception.user.InvalidUserParameterException;
import com.lukesauls.studycompanion.studycompanion_backend.exception.user.UserException;
import com.lukesauls.studycompanion.studycompanion_backend.exception.user.UserNotFoundException;
import com.lukesauls.studycompanion.studycompanion_backend.exception.user.UserOperationException;
import com.lukesauls.studycompanion.studycompanion_backend.model.postgres.Deck;
import com.lukesauls.studycompanion.studycompanion_backend.model.postgres.User;
import com.lukesauls.studycompanion.studycompanion_backend.repository.postgres.DeckRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DeckService {

    private static final Logger logger = LoggerFactory.getLogger(DeckService.class);

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private UserService userService;

    /**
     * Creates a new deck for the specified user.
     * Validates that both title and description are not empty.
     * Maintains bidirectional relationship by adding deck to user's collection.
     * 
     * @param deckDto the deck creation data containing userId, title, and
     *                description
     * @return the created deck with generated ID and timestamps
     * @throws InvalidDeckParameterException if any nonnull arg is null
     * @throws InvalidUserParameterException if userId is invalid
     * @throws UserNotFoundException         if the specified user does not exist
     * @throws InvalidDeckCreationException  if title or description is blank
     * @throws UserOperationException        if user operations fail
     * @throws DeckOperationException        if server error occurs
     */
    @SuppressWarnings({ "null", "unused" })
    public @NonNull Deck createDeck(@NonNull DeckDto.Create deckDto) {
        if (deckDto == null) {
            throw new InvalidDeckParameterException("Deck data transfer object cannot be null");
        }

        if (deckDto.title().trim().isEmpty()) {
            throw new InvalidDeckCreationException("Title cannot be empty");
        }

        if (deckDto.description().trim().isEmpty()) {
            throw new InvalidDeckCreationException("Description cannot be empty");
        }

        try {
            User user = userService.getUserById(deckDto.userId());

            Deck deck = new Deck(user, deckDto.title().trim(), deckDto.description().trim());

            user.addDeck(deck);

            return deckRepository.save(deck);
        } catch (UserException e) {
            logger.error("Deck creation failed due to user issue: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to create a new deck: {}", e.getMessage());
            throw new DeckOperationException("Failed to create a new deck", e);
        }
    }

    /**
     * Retrieves a deck by its unique identifier.
     * 
     * @param id the UUID of the deck to retrieve
     * @return the deck with the specified ID
     * @throws InvalidDeckParameterException if any nonnull arg is null
     * @throws DeckNotFoundException         if no deck exists with the given ID
     * @throws DeckOperationException        if server error occurs
     */
    @SuppressWarnings({ "null", "unused" })
    public @NonNull Deck getDeckById(@NonNull UUID id) {
        if (id == null) {
            throw new InvalidDeckParameterException("ID cannot not be null");
        }

        try {
            logger.debug("Checking if deck exists by provided ID");
            if (!deckRepository.existsById(id)) {
                throw new DeckNotFoundException("Deck with ID " + id + " not found");
            }

            Deck deck = deckRepository.findById(id).get();
            logger.info("Found deck with provided ID");
            return deck;
        } catch (DeckNotFoundException e) {
            logger.error("Deck not found with provided ID: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to fetch deck: {}", e.getMessage());
            throw new DeckOperationException("Failed to fetch deck", e);
        }
    }

    /**
     * Retrieves all decks in the system.
     * Currently unrestricted - should be limited to admin users in production.
     * 
     * @return a list of all decks in the system
     * @throws DeckOperationException if server error occurs
     */
    // FIXME: Add requestUUID and only fetch if UUID belongs to an admin
    public List<Deck> getAllDecks() {
        try {
            logger.debug("Fetching all decks");
            List<Deck> decks = deckRepository.findAll();
            logger.info("Successfully fetched all decks");
            return decks;
        } catch (Exception e) {
            logger.error("Failed to fetch all decks: {}", e.getMessage());
            throw new DeckOperationException("Failed to fetch all decks", e);
        }
    }

    /**
     * Retrieves all decks belonging to a specific user.
     * 
     * @param userId the UUID of the user whose decks to retrieve
     * @return a list of decks owned by the user, empty if user has no decks
     * @throws InvalidDeckParameterException if any nonnull arg is null
     * @throws DeckOperationException        if server error occurs
     */
    @SuppressWarnings("unused")
    public List<Deck> getAllUserDecks(@NonNull UUID userId) {
        if (userId == null) {
            throw new InvalidDeckParameterException("userId cannot be null");
        }

        try {
            logger.debug("Fetching all decks belonging to the provided user");
            List<Deck> decks = deckRepository.findByUserId(userId);
            logger.info("Fetched all decks belonging to the provided user");
            return decks;
        } catch (Exception e) {
            logger.error("Failed to fetch all decks belonging to the user with this id: {}", e.getMessage());
            throw new DeckOperationException("Failed to fetch all decks belonging to the user with this id", e);
        }
    }

    /**
     * Counts the total number of decks belonging to a specific user.
     * 
     * @param userId the UUID of the user whose deck count to retrieve
     * @return the number of decks owned by the user
     * @throws InvalidDeckParameterException if any nonnull arg is null
     * @throws DeckOperationException        if server error occurs
     */
    @SuppressWarnings("unused")
    public long getCountOfAllUserDecks(@NonNull UUID userId) {
        if (userId == null) {
            throw new InvalidDeckParameterException("userId cannot be null");
        }

        try {
            logger.debug("Counting how many decks the provided user has");
            long deckCount = deckRepository.countByUserId(userId);
            logger.info("This user has {} decks", deckCount);
            return deckCount;
        } catch (Exception e) {
            logger.error("Failed to calculate user's decks: {}", e.getMessage());
            throw new DeckOperationException("Failed to calculate user's decks", e);
        }
    }

    /**
     * Updates an existing deck's title and/or description.
     * Only the deck owner can perform this operation.
     * At least one field must be provided for update.
     * 
     * @param deckId           the UUID of the deck to update
     * @param deckDto          the update data containing new title and/or
     *                         description
     * @param requestingUserId the UUID of the user making the request
     * @return the updated deck
     * @throws InvalidDeckParameterException   if any nonnull arg is null
     * @throws DeckNotFoundException           if the deck does not exist
     * @throws InvalidDeckUpdateException      if no valid fields are provided for
     *                                         update
     * @throws UnauthorizedDeckAccessException if the requesting user is not the
     *                                         deck owner
     * @throws DeckOperationException          if server error occurs
     */
    @SuppressWarnings("unused")
    public Deck updateDeck(@NonNull UUID deckId, @NonNull DeckDto.Update deckDto, @NonNull UUID requestingUserId) {
        if (deckId == null) {
            throw new InvalidDeckParameterException("deckId cannot be null");
        }

        if (deckDto == null) {
            throw new InvalidDeckParameterException("Deck data transfer object cannot be null");
        }

        if (requestingUserId == null) {
            throw new InvalidDeckParameterException("Requesting userId cannot be null");
        }

        boolean titleProvided = deckDto.title() != null && !deckDto.title().trim().isEmpty();
        boolean descriptionProvided = deckDto.description() != null && !deckDto.description().trim().isEmpty();

        if (!titleProvided && !descriptionProvided) {
            throw new InvalidDeckUpdateException("At least one field must be provided for update");
        }

        try {
            Deck existingDeck = getDeckById(deckId);

            logger.debug("Checking if user requesting this deck update is authorized");
            if (!existingDeck.getUser().getId().equals(requestingUserId)) {
                throw new UnauthorizedDeckAccessException("You can only update your own decks");
            }

            if (titleProvided) {
                existingDeck.setTitle(deckDto.title().trim());
            }

            if (descriptionProvided) {
                existingDeck.setDescription(deckDto.description().trim());
            }

            logger.debug("Updating deck");
            Deck updatedDeck = deckRepository.save(existingDeck);
            logger.info("Successfully updated deck");
            return updatedDeck;
        } catch (DeckException e) {
            logger.error("Deck update failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to update deck: {}", e.getMessage());
            throw new DeckOperationException("Failed to update deck", e);
        }
    }

    /**
     * Deletes a deck by its unique identifier.
     * Only the deck owner can perform this operation.
     * Maintains bidirectional relationship by removing deck from user's collection.
     * 
     * @param deckId           the UUID of the deck to delete
     * @param requestingUserId the UUID of the user making the request
     * @throws InvalidDeckParameterException   if any nonnull arg is null
     * @throws DeckNotFoundException           if the deck does not exist
     * @throws UnauthorizedDeckAccessException if the requesting user is not the
     *                                         deck owner
     * @throws DeckOperationException          if server error occurs
     */
    @SuppressWarnings("unused")
    public void deleteDeckById(@NonNull UUID deckId, @NonNull UUID requestingUserId) {
        if (deckId == null) {
            throw new InvalidDeckParameterException("deckId cannot be null");
        }

        if (requestingUserId == null) {
            throw new InvalidDeckParameterException("Requesting userId cannot be null");
        }

        try {
            Deck deck = getDeckById(deckId);

            logger.debug("Checking if user requesting this deck delete is authorized");
            if (!deck.getUser().getId().equals(requestingUserId)) {
                throw new UnauthorizedDeckAccessException("You can only delete your own decks");
            }

            logger.debug("Fetching the deck's owner");
            User user = deck.getUser();
            logger.debug("Disconnecting the bidirectional connection between this deck and its owner");
            user.removeDeck(deck);

            logger.debug("Removed connection, deleting deck now");
            deckRepository.deleteById(deckId);
            logger.info("Successfully deleted deck");
        } catch (DeckException e) {
            logger.error("Deck deletion");
            throw e;
        } catch (Exception e) {
            logger.error("Failed to delete deck: {}", e.getMessage());
            throw new DeckOperationException("Failed to delete deck", e);
        }
    }

    /**
     * Deletes all decks belonging to a specific user.
     * This is typically used for administrative purposes or account deletion.
     * Maintains bidirectional relationship by clearing user's deck collection.
     * Currently unrestricted - should be limited to admin users in production.
     * 
     * @param userId the UUID of the user whose decks to delete
     * @throws InvalidDeckParameterException if any nonnull arg is null
     * @throws InvalidUserParameterException if userId is invalid
     * @throws UserNotFoundException         if the specified user does not exist
     * @throws UserOperationException        if user operations fail
     * @throws DeckOperationException        if server error occurs
     */
    // FIXME: Add requestUUID and only delete if UUID belongs to an admin
    @SuppressWarnings("unused")
    public void deleteAllUserDecks(@NonNull UUID userId) {
        if (userId == null) {
            throw new InvalidDeckParameterException("userId cannot be null");
        }

        try {
            User user = userService.getUserById(userId);

            logger.debug("Clearing user's decks");
            user.getDecks().clear();

            logger.debug("Deleting all decks belonging to the associated user");
            deckRepository.deleteByUserId(userId);
            logger.info("Successfully deleted all decks belonging to the associated user");
        } catch (UserException e) {
            logger.error("Deck deletions failed due to user issue: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to delete decks: {}", e.getMessage());
            throw new DeckOperationException("Failed to delete decks", e);
        }
    }
}
