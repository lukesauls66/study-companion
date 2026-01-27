package com.study_companion.backend.repository.postgres;

import com.study_companion.backend.model.postgres.Deck;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface DeckRepository extends JpaRepository<Deck, UUID> {
    
    /**
     * Find all decks belonging to a specific user
     */
    List<Deck> findByUserId(UUID userId);

    /**
     * Delete all decks belonging to a specific user
     */
    void deleteByUserId(UUID userId);

    /**
     * Count number of decks belonging to a specific user
     */
    long countByUserId(UUID userId);
}
