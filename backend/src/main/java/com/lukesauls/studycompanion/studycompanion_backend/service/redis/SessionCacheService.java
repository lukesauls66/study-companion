package com.lukesauls.studycompanion.studycompanion_backend.service.redis;

import com.lukesauls.studycompanion.studycompanion_backend.model.redis.Session;
import com.lukesauls.studycompanion.studycompanion_backend.model.redis.CachedDecks;
import com.lukesauls.studycompanion.studycompanion_backend.repository.redis.SessionRepository;
import com.lukesauls.studycompanion.studycompanion_backend.repository.redis.CachedDecksRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.UUID;

@Service
public class SessionCacheService {
    
    @Autowired
    private SessionRepository sessionRepository;
    
    @Autowired
    private CachedDecksRepository cachedDecksRepository;
    
    /**
     * Logout user and cleanup both session and cache
     */
    public void logout(UUID userId) {
        sessionRepository.deleteByUserId(userId);
        cachedDecksRepository.deleteById(userId);
    }
    
    /**
     * Validate session and cleanup orphaned cache if needed
     * @return true if user has valid session, false otherwise
     */
    public boolean validateSessionAndCleanupCache(UUID userId) {
        Optional<Session> session = sessionRepository.findByUserId(userId);
        
        if (session.isEmpty() || session.get().isExpired()) {
            // No valid session - cleanup orphaned cache
            cachedDecksRepository.deleteById(userId);
            return false;
        }
        
        return true;
    }
    
    /**
     * Get user's cached decks only if they have valid session
     */
    public Optional<CachedDecks> getCachedDecksIfSessionValid(UUID userId) {
        if (!validateSessionAndCleanupCache(userId)) {
            return Optional.empty();
        }
        
        return cachedDecksRepository.findById(userId);
    }
}