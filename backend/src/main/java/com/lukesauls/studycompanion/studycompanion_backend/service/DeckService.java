package com.lukesauls.studycompanion.studycompanion_backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.lukesauls.studycompanion.studycompanion_backend.dto.DeckDto;
import com.lukesauls.studycompanion.studycompanion_backend.exception.DeckNotFoundException;
import com.lukesauls.studycompanion.studycompanion_backend.exception.InvalidDeckCreationException;
import com.lukesauls.studycompanion.studycompanion_backend.exception.InvalidDeckUpdateException;
import com.lukesauls.studycompanion.studycompanion_backend.exception.UnauthorizedDeckAccessException;
import com.lukesauls.studycompanion.studycompanion_backend.model.postgres.Deck;
import com.lukesauls.studycompanion.studycompanion_backend.model.postgres.User;
import com.lukesauls.studycompanion.studycompanion_backend.repository.postgres.DeckRepository;

@Service
public class DeckService {

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private UserService userService;

    /**
     * Creates a new deck for the specified user.
     * Validates that both title and description are not empty.
     * Maintains bidirectional relationship by adding deck to user's collection.
     * 
     * @param deckDto the deck creation data containing userId, title, and description
     * @return the created deck with generated ID and timestamps
     * @throws InvalidDeckCreationException if title or description is blank
     * @throws UserNotFoundException if the specified user does not exist
     */
    @SuppressWarnings("null")
    public @NonNull Deck createDeck(@NonNull DeckDto.Create deckDto) {
        User user = userService.getUserById(deckDto.userId());

        if (deckDto.title().trim().isEmpty()) {
            throw new InvalidDeckCreationException("Title cannot be empty");
        }

        if (deckDto.description().trim().isEmpty()) {
            throw new InvalidDeckCreationException("Description cannot be empty");
        }

        Deck deck = new Deck(user, deckDto.title().trim(), deckDto.description().trim());
        
        user.addDeck(deck);

        return deckRepository.save(deck);
    }

    /**
     * Retrieves a deck by its unique identifier.
     * 
     * @param id the UUID of the deck to retrieve
     * @return the deck with the specified ID
     * @throws DeckNotFoundException if no deck exists with the given ID
     */
    @SuppressWarnings("null")
    public @NonNull Deck getDeckById(@NonNull UUID id) {
        if (!deckRepository.existsById(id)) {
            throw new DeckNotFoundException("Deck with ID " + id + " not found");
        }

        return deckRepository.findById(id).get();
    }

    /**
     * Retrieves all decks in the system.
     * Currently unrestricted - should be limited to admin users in production.
     * 
     * @return a list of all decks in the system
     */
    //FIXME: Add requestUUID and only fetch if UUID belongs to an admin
    public List<Deck> getAllDecks() {
        return deckRepository.findAll();
    }

    /**
     * Retrieves all decks belonging to a specific user.
     * 
     * @param userId the UUID of the user whose decks to retrieve
     * @return a list of decks owned by the user, empty if user has no decks
     */
    public List<Deck> getAllUserDecks(@NonNull UUID userId) {
        return deckRepository.findByUserId(userId);
    }

    /**
     * Counts the total number of decks belonging to a specific user.
     * 
     * @param userId the UUID of the user whose deck count to retrieve
     * @return the number of decks owned by the user
     */
    public long getCountOfAllUserDecks(@NonNull UUID userId) {
        return deckRepository.countByUserId(userId);
    }

    /**
     * Updates an existing deck's title and/or description.
     * Only the deck owner can perform this operation.
     * At least one field must be provided for update.
     * 
     * @param deckId the UUID of the deck to update
     * @param deckDto the update data containing new title and/or description
     * @param requestingUserId the UUID of the user making the request
     * @return the updated deck
     * @throws DeckNotFoundException if the deck does not exist
     * @throws InvalidDeckUpdateException if no valid fields are provided for update
     * @throws UnauthorizedDeckAccessException if the requesting user is not the deck owner
     */
    public Deck updateDeck(@NonNull UUID deckId, @NonNull DeckDto.Update deckDto, @NonNull UUID requestingUserId) {
        boolean titleProvided = deckDto.title() != null && !deckDto.title().trim().isEmpty();
        boolean descriptionProvided = deckDto.description() != null && !deckDto.description().trim().isEmpty();
        
        if (!titleProvided && !descriptionProvided) {
            throw new InvalidDeckUpdateException("At least one field must be provided for update");
        }
        
        Deck existingDeck = getDeckById(deckId);

        if (!existingDeck.getUser().getId().equals(requestingUserId)) {
            throw new UnauthorizedDeckAccessException("You can only update your own decks");
        }

        if (titleProvided) {
            existingDeck.setTitle(deckDto.title().trim());
        }

        if (descriptionProvided) {
            existingDeck.setDescription(deckDto.description().trim());
        }

        return deckRepository.save(existingDeck);
    }

    /**
     * Deletes a deck by its unique identifier.
     * Only the deck owner can perform this operation.
     * Maintains bidirectional relationship by removing deck from user's collection.
     * 
     * @param deckId the UUID of the deck to delete
     * @param requestingUserId the UUID of the user making the request
     * @throws DeckNotFoundException if the deck does not exist
     * @throws UnauthorizedDeckAccessException if the requesting user is not the deck owner
     */
    public void deleteDeckById(@NonNull UUID deckId, @NonNull UUID requestingUserId) {
        Deck deck = getDeckById(deckId);

        if (!deck.getUser().getId().equals(requestingUserId)) {
            throw new UnauthorizedDeckAccessException("You can only delete your own decks");
        }

        User user = deck.getUser();
        user.removeDeck(deck);

        deckRepository.deleteById(deckId);
    }

    /**
     * Deletes all decks belonging to a specific user.
     * This is typically used for administrative purposes or account deletion.
     * Maintains bidirectional relationship by clearing user's deck collection.
     * Currently unrestricted - should be limited to admin users in production.
     * 
     * @param userId the UUID of the user whose decks to delete
     * @throws UserNotFoundException if the specified user does not exist
     */
    //FIXME: Add requestUUID and only delete if UUID belongs to an admin
    public void deleteAllUserDecks(@NonNull UUID userId) {
        User user = userService.getUserById(userId);
        
        user.getDecks().clear();
        
        deckRepository.deleteByUserId(userId);
    }
}
