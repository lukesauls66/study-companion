package com.lukesauls.studycompanion.studycompanion_backend.repository.redis;

import com.lukesauls.studycompanion.studycompanion_backend.model.redis.Session;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionRepository extends CrudRepository<Session, String> {
    
    /**
     * Find the single session for a specific user
     */
    Optional<Session> findByUserId(UUID userId);
    
    /**
     * Delete the session for a specific user
     */
    void deleteByUserId(UUID userId);
    
    /**
     * Check if a user has an active session
     */
    boolean existsByUserId(UUID userId);
}