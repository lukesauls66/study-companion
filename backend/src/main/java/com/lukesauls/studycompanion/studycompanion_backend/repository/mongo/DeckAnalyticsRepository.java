package com.lukesauls.studycompanion.studycompanion_backend.repository.mongo;

import java.util.List;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.lukesauls.studycompanion.studycompanion_backend.model.mongo.DeckAnalytics;

public interface DeckAnalyticsRepository extends MongoRepository<DeckAnalytics, UUID> {
    
    /**
     * Find all deck analytics based by deck ID
     */
    List<DeckAnalytics> findByDeckId(UUID deckId);

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
