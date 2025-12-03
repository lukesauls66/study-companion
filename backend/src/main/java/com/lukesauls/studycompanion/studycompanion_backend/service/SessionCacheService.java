package com.lukesauls.studycompanion.studycompanion_backend.service;

import com.lukesauls.studycompanion.studycompanion_backend.model.redis.Session;
import com.lukesauls.studycompanion.studycompanion_backend.model.redis.CachedDecks;
import com.lukesauls.studycompanion.studycompanion_backend.repository.redis.SessionRepository;
import com.lukesauls.studycompanion.studycompanion_backend.repository.redis.CachedDecksRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.lang.NonNull;
import java.util.Optional;
import java.util.UUID;

@Service
public class SessionCacheService {
    
    @Autowired
    private SessionRepository sessionRepository;
    
    @Autowired
    private CachedDecksRepository cachedDecksRepository;
    
    /**
     * Logs out a user by removing their session and clearing their cached data.
     * Performs complete cleanup of both Redis session and cache stores.
     * 
     * @param userId the UUID of the user to log out
     */
    public void logout(@NonNull UUID userId) {
        sessionRepository.deleteByUserId(userId);
        cachedDecksRepository.deleteById(userId);
    }
    
    /**
     * Validates a user's session and performs cleanup if session is invalid or expired.
     * Automatically removes orphaned cache data when session is not found or expired.
     * 
     * @param userId the UUID of the user whose session to validate
     * @return true if user has valid, non-expired session; false otherwise
     */
    public boolean validateSessionAndCleanupCache(@NonNull UUID userId) {
        Optional<Session> session = sessionRepository.findByUserId(userId);
        
        if (session.isEmpty()) {
            cachedDecksRepository.deleteById(userId);
            return false;
        } else if (session.get().isExpired()) {
            sessionRepository.deleteByUserId(userId);
            cachedDecksRepository.deleteById(userId);
            return false;
        }
        
        return true;
    }
    
    /**
     * Retrieves a user's cached decks only if they have a valid session.
     * Performs session validation first and returns empty if session is invalid.
     * This ensures cached data is only accessible with valid authentication.
     * 
     * @param userId the UUID of the user whose cached decks to retrieve
     * @return Optional containing cached decks if session is valid, empty otherwise
     */
    public Optional<CachedDecks> getCachedDecksIfSessionValid(@NonNull UUID userId) {
        if (!validateSessionAndCleanupCache(userId)) {
            return Optional.empty();
        }
        
        return cachedDecksRepository.findById(userId);
    }
}