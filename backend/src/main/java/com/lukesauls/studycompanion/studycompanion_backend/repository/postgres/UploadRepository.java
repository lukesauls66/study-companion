package com.lukesauls.studycompanion.studycompanion_backend.repository.postgres;

import com.lukesauls.studycompanion.studycompanion_backend.model.postgres.Upload;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface UploadRepository extends JpaRepository<Upload, UUID> {
    
    /**
     * Find all uploads belonging to a specific deck
     */
    List<Upload> findByDeckId(UUID deckId);

    /**
     * Delete all uploads belonging to a specific deck
     */
    void deleteByDeckId(UUID deckId);

    /**
     * Count the number of uploads belonging to a specific deck
     */
    long countByDeckId(UUID deckId);

    /**
     * Find all uploads belonging to a specific user
     */
    List<Upload> findByUserId(UUID userId);

    /**
     * Delete all uploads belonging to a specific user
     */
    void deleteByUserId(UUID userId);

    /**
     * Count the number of uploads belonging to a specific user
     */
    long countByUserId(UUID userId);
}
