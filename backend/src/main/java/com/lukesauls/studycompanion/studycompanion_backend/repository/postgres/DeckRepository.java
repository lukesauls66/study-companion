package com.lukesauls.studycompanion.studycompanion_backend.repository.postgres;

import com.lukesauls.studycompanion.studycompanion_backend.model.postgres.Deck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.List;

@Repository
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
