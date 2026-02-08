package com.study_companion.backend.repository.mongo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.study_companion.backend.model.mongo.DeckAnalytics;

public interface DeckAnalyticsRepository extends MongoRepository<DeckAnalytics, UUID> {
    
    /**
     * Find all deck analytics based by deck ID
     */
    List<DeckAnalytics> findByDeckId(UUID deckId);

    /**
     * Find deck analytics by deck ID and user ID
     * Returns at most one record since there should be only one analytics record per user-deck combination
     */
    Optional<DeckAnalytics> findByDeckIdAndUserId(UUID deckId, UUID userId);

    /**
     * Delete all deck analytics by deck ID
     */
    void deleteByDeckId(UUID deckId);

    /**
     * Count number of deck analytics by deck ID
     */
    long countByDeckId(UUID deckId);

    /**
     * Find all deck analytics based by user ID
     */
    List<DeckAnalytics> findByUserId(UUID userId);

    /**
     * Find all deck analytics based on user ID based on highest score in descending order
     */
    List<DeckAnalytics> findByUserIdOrderByHighestScoreDesc(UUID userId);

    /**
     * Delete all deck analytics by user ID
     */
    void deleteByUserId(UUID userId);

    /**
     * Count number of deck analytics by user ID
     */
    long countByUserId(UUID userId);

    /**
     * Find all deck analytics by user with a proficient score
     */
    List<DeckAnalytics> findByUserIdAndProficiency(UUID userId, boolean proficiency);
}
