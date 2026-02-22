package com.study_companion.backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.study_companion.backend.dto.DeckDto;
import com.study_companion.backend.exception.deck.DeckException;
import com.study_companion.backend.exception.deck.DeckNotFoundException;
import com.study_companion.backend.exception.deck.DeckOperationException;
import com.study_companion.backend.exception.deck.InvalidDeckCreationException;
import com.study_companion.backend.exception.deck.InvalidDeckParameterException;
import com.study_companion.backend.exception.deck.InvalidDeckUpdateException;
import com.study_companion.backend.exception.deck.UnauthorizedDeckAccessException;
import com.study_companion.backend.exception.user.InvalidUserParameterException;
import com.study_companion.backend.exception.user.UnauthorizedUserAccessException;
import com.study_companion.backend.exception.user.UserException;
import com.study_companion.backend.exception.user.UserNotFoundException;
import com.study_companion.backend.exception.user.UserOperationException;
import com.study_companion.backend.model.postgres.Deck;
import com.study_companion.backend.model.postgres.User;
import com.study_companion.backend.repository.postgres.DeckRepository;
import com.study_companion.backend.repository.postgres.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DeckService {

    private static final Logger logger = LoggerFactory.getLogger(DeckService.class);

    private final DeckRepository deckRepository;

    private final UserRepository userRepository;

    DeckService(DeckRepository deckRepository, UserRepository userRepository) {
        this.deckRepository = deckRepository;
        this.userRepository = userRepository;
    }

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
    public Deck createDeck(DeckDto.Create deckDto) {
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
            User user = userRepository.findById(deckDto.userId())
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

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
    public Deck getDeckById(UUID id) {
        if (id == null) {
            throw new InvalidDeckParameterException("ID cannot not be null");
        }

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null) {
                throw new UnauthorizedUserAccessException("Authentication required");
            }

            logger.debug("Checking if deck exists by provided ID");
            if (!deckRepository.existsById(id)) {
                throw new DeckNotFoundException("Deck with ID " + id + " not found");
            }
            Deck deck = deckRepository.findById(id).get();
            logger.info("Found deck with provided ID");

            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            logger.debug("Checking if user is admin or deck owner");
            if (!isAdmin && deck.getUser().getId() != UUID.fromString(authentication.getName())) {
                throw new UnauthorizedUserAccessException("Unauthorized user access");
            }

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
     * @throws UnauthorizedDeckAccessException if authorization fails or user is not
     *                                         an admin
     * @throws DeckOperationException          if server error occurs
     */
    public List<Deck> getAllDecks() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null) {
                throw new UnauthorizedDeckAccessException("Authentication required");
            }

            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            logger.debug("Checking if user is admin");
            if (!isAdmin) {
                throw new UnauthorizedDeckAccessException("Unauthorized user access");
            }

            logger.debug("Fetching all decks");
            List<Deck> decks = deckRepository.findAll();
            logger.info("Successfully fetched all decks");
            return decks;
        } catch (DeckException e) {
            logger.error("Deck operation failed: {}", e.getMessage());
            throw e;
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
    public List<Deck> getAllUserDecks(UUID userId) {
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
    public long getCountOfAllUserDecks(UUID userId) {
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
    public Deck updateDeck(UUID deckId, DeckDto.Update deckDto, UUID requestingUserId) {
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
    public void deleteDeckById(UUID deckId, UUID requestingUserId) {
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
    public void deleteAllUserDecks(UUID userId) {
        if (userId == null) {
            throw new InvalidDeckParameterException("userId cannot be null");
        }

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null) {
                throw new UnauthorizedDeckAccessException("Authentication required");
            }

            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            logger.debug("Checking if user is admin");
            if (!isAdmin) {
                throw new UnauthorizedDeckAccessException("Unauthorized user access");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            logger.debug("Clearing user's decks");
            user.getDecks().clear();

            logger.debug("Deleting all decks belonging to the associated user");
            deckRepository.deleteByUserId(userId);
            logger.info("Successfully deleted all decks belonging to the associated user");
        } catch (UserException e) {
            logger.error("Deck deletions failed due to user issue: {}", e.getMessage());
            throw e;
        } catch (DeckException e) {
            logger.error("Deck operation failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to delete decks: {}", e.getMessage());
            throw new DeckOperationException("Failed to delete decks", e);
        }
    }
}
