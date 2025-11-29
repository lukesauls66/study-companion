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
     * Logout user and cleanup both session and cache
     */
    public void logout(@NonNull UUID userId) {
        sessionRepository.deleteByUserId(userId);
        cachedDecksRepository.deleteById(userId);
    }
    
    /**
     * Validate session and cleanup orphaned cache if needed
     * @return true if user has valid session, false otherwise
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
     * Get user's cached decks only if they have valid session
     */
    public Optional<CachedDecks> getCachedDecksIfSessionValid(@NonNull UUID userId) {
        if (!validateSessionAndCleanupCache(userId)) {
            return Optional.empty();
        }
        
        return cachedDecksRepository.findById(userId);
    }
}