package com.lukesauls.studycompanion.studycompanion_backend.repository.redis;

import com.lukesauls.studycompanion.studycompanion_backend.model.redis.CachedDecks;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface CachedDecksRepository extends CrudRepository<CachedDecks, UUID> {

    /**
     * Check if cached decks exist for a user
     */
    boolean existsByUserId(UUID userId);
}