package com.study_companion.backend.repository.postgres;


import com.study_companion.backend.model.postgres.Card;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID> {
    
    /**
     * Find all cards belonging to a specific deck
     */
    List<Card> findByDeckId(UUID deckId);

    /**
     * Count the number of cards in a specific deck
     */
    long countByDeckId(UUID deckId);

    /**
     * Delete all cards belonging to a specific deck
     */
    void deleteByDeckId(UUID deckId);
}
