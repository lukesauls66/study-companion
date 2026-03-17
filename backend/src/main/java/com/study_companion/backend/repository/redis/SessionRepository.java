package com.study_companion.backend.repository.redis;

import com.study_companion.backend.model.redis.Session;
import org.springframework.data.repository.CrudRepository;
import java.util.Optional;
import java.util.UUID;

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