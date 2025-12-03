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
     * Create new deck
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

        Deck deck = new Deck(user, deckDto.title(), deckDto.description());
        
        user.addDeck(deck);

        return deckRepository.save(deck);
    }

    /**
     * Get deck by ID if deck exists
     */
    @SuppressWarnings("null")
    public @NonNull Deck getDeckById(@NonNull UUID id) {
        if (!deckRepository.existsById(id)) {
            throw new DeckNotFoundException("Deck with ID " + id + " not found");
        }

        return deckRepository.findById(id).get();
    }

    /**
     * Get all decks
     */
    //FIXME: Add requestUUID and only fetch if UUID belongs to an admin
    public List<Deck> getAllDecks() {
        return deckRepository.findAll();
    }

    /**
     * Get all decks belonging to a user
     */
    public List<Deck> getAllUserDecks(@NonNull UUID userId) {
        return deckRepository.findByUserId(userId);
    }

    /**
     * Get count of all decks belonging to a user
     */
    public long getCountOfAllUserDecks(@NonNull UUID userId) {
        return deckRepository.countByUserId(userId);
    }

    /**
     * Update deck
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
     * Delete deck only if logged in user owns deck
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
     * Delete all decks belonging to a user
     */
    //FIXME: Add requestUUID and only delete if UUID belongs to an admin
    public void deleteAllUserDecks(@NonNull UUID userId) {
        User user = userService.getUserById(userId);
        
        user.getDecks().clear();
        
        deckRepository.deleteByUserId(userId);
    }
}
