package com.study_companion.backend.repository.mongo;

import java.util.List;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.study_companion.backend.model.mongo.ReviewSession;

public interface ReviewSessionRepository extends MongoRepository<ReviewSession, UUID> {

    /**
     * Find all review sessions based by deck ID
     */
    List<ReviewSession> findByDeckId(UUID deckId);

    /**
     * Delete all review sessions by deck ID
     */
    void deleteByDeckId(UUID deckId);

    /**
     * Count number of review sessions by deck ID
     */
    long countByDeckId(UUID deckId);

    /**
     * Find latest session for a deck
     */
    ReviewSession findTopByDeckIdOrderByDateDesc(UUID deckId);

    /**
     * Find all review sessions based by user ID
     */
    List<ReviewSession> findByUserId(UUID userId);

    /**
     * Delete all review sessions by user ID
     */
    void deleteByUserId(UUID userId);

    /**
     * Count number of review sessions by user ID
     */
    long countByUserId(UUID userId);
}
