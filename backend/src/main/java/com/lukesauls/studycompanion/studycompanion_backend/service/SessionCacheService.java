package com.lukesauls.studycompanion.studycompanion_backend.service;

import com.lukesauls.studycompanion.studycompanion_backend.model.redis.Session;
import com.lukesauls.studycompanion.studycompanion_backend.model.redis.CachedDecks;
import com.lukesauls.studycompanion.studycompanion_backend.model.redis.DeckCache;
import com.lukesauls.studycompanion.studycompanion_backend.repository.redis.SessionRepository;
import com.lukesauls.studycompanion.studycompanion_backend.repository.redis.CachedDecksRepository;
import com.lukesauls.studycompanion.studycompanion_backend.exception.session.SessionOperationException;
import com.lukesauls.studycompanion.studycompanion_backend.exception.session.CacheOperationException;
import com.lukesauls.studycompanion.studycompanion_backend.exception.session.InvalidSessionParameterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.lang.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SessionCacheService {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionCacheService.class);
    
    @Autowired
    private SessionRepository sessionRepository;
    
    @Autowired
    private CachedDecksRepository cachedDecksRepository;
    
    /**
     * Creates a new session for a user.
     * If a session already exists, it will be replaced.
     * 
     * @param userId the UUID of the user to create a session for
     * @param ttlSeconds the number of seconds until session expires, or null for default 24-hour expiration
     * @return the created session
     * @throws InvalidSessionParameterException if userId is null or ttlSeconds is invalid
     * @throws SessionOperationException if Redis operation fails
     */
    @SuppressWarnings("unused")
    public Session createSession(@NonNull UUID userId, Long ttlSeconds) {
        if (userId == null) {
            throw new InvalidSessionParameterException("User ID cannot be null when creating a session");
        }
        
        if (ttlSeconds != null && ttlSeconds <= 0) {
            throw new InvalidSessionParameterException("TTL must be positive when specified, got: " + ttlSeconds);
        }
        
        try {
            logger.debug("Creating session for user ID: {}", userId);
            Session session = ttlSeconds != null ? new Session(userId, ttlSeconds) : new Session(userId);
            Session savedSession = sessionRepository.save(session);
            logger.info("Successfully created session {} for user {}", savedSession.getSessionId(), userId);
            return savedSession;
        } catch (Exception e) {
            logger.error("Failed to create session for user: {}", e.getMessage());
            throw new SessionOperationException("Failed to create session for user: " + userId, e);
        }
    }

    /**
     * Creates or updates cached decks for a user.
     * If cached decks exist, updates them with new data.
     * If no cached decks exist, creates a new cache entry.
     * This improves performance by storing frequently accessed deck data in Redis.
     * 
     * @param userId the UUID of the user whose decks to cache
     * @param deckCaches the list of deck cache objects to store
     * @return the created or updated cached decks
     * @throws InvalidSessionParameterException if userId is null
     * @throws CacheOperationException if Redis operation fails
     */
    @SuppressWarnings("unused")
    public CachedDecks createOrUpdateCachedDecks(@NonNull UUID userId, List<DeckCache> deckCaches) {
        if (userId == null) {
            throw new InvalidSessionParameterException("User ID cannot be null when caching decks");
        }
        
        try {
            logger.debug("Creating or updating cached decks for user ID: {}", userId);
            Optional<CachedDecks> existingCache = cachedDecksRepository.findById(userId);
            
            if (existingCache.isPresent()) {
                CachedDecks cachedDecks = existingCache.get();
                
                cachedDecks.getDecks().clear();
                if (deckCaches != null) {
                    for (DeckCache deckCache : deckCaches) {
                        cachedDecks.addDeck(deckCache);
                    }
                }
                
                CachedDecks savedCache = cachedDecksRepository.save(cachedDecks);
                logger.info("Successfully updated cached decks for user {}", userId);
                return savedCache;
            } else {
                CachedDecks cachedDecks = new CachedDecks(userId, deckCaches);
                CachedDecks savedCache = cachedDecksRepository.save(cachedDecks);
                logger.info("Successfully created cached decks for user {}", userId);
                return savedCache;
            }
        } catch (Exception e) {
            logger.error("Failed to create or update cached decks for user {}: {}", userId, e.getMessage());
            throw new CacheOperationException("Failed to cache decks for user: " + userId, e);
        }
    }

    /**
     * Updates a specific deck in the user's cache with new data.
     * If the deck doesn't exist in cache, adds it.
     * If no cache exists for user, creates new cache with this deck.
     * 
     * @param userId the UUID of the user whose deck cache to update
     * @param deckId the UUID of the specific deck to update
     * @param updatedDeckCache the new deck cache data to replace the existing deck
     * @return the updated cached decks
     * @throws InvalidSessionParameterException if any required parameter is null
     * @throws CacheOperationException if Redis operation fails
     */
    @SuppressWarnings("unused")
    public CachedDecks updateSpecificDeckInCache(@NonNull UUID userId, @NonNull UUID deckId, @NonNull DeckCache updatedDeckCache) {
        if (userId == null) {
            throw new InvalidSessionParameterException("User ID cannot be null when updating deck cache");
        }
        
        if (deckId == null) {
            throw new InvalidSessionParameterException("Deck ID cannot be null when updating deck cache");
        }
        
        if (updatedDeckCache == null) {
            throw new InvalidSessionParameterException("Updated deck cache cannot be null");
        }
        
        try {
            logger.debug("Updating specific deck {} in cache for user {}", deckId, userId);
            Optional<CachedDecks> existingCache = cachedDecksRepository.findById(userId);
            
            if (existingCache.isPresent()) {
                CachedDecks cachedDecks = existingCache.get();
                
                DeckCache existingDeck = cachedDecks.findDeckById(deckId);
                if (existingDeck != null) {
                    cachedDecks.updateDeck(updatedDeckCache);
                    logger.debug("Updated existing deck {} in cache", deckId);
                } else {
                    cachedDecks.addDeck(updatedDeckCache);
                    logger.debug("Added new deck {} to cache", deckId);
                }
                
                CachedDecks savedCache = cachedDecksRepository.save(cachedDecks);
                logger.info("Successfully updated deck {} in cache for user {}", deckId, userId);
                return savedCache;
            } else {
                CachedDecks cachedDecks = new CachedDecks(userId);
                cachedDecks.addDeck(updatedDeckCache);
                CachedDecks savedCache = cachedDecksRepository.save(cachedDecks);
                logger.info("Created new cache with deck {} for user {}", deckId, userId);
                return savedCache;
            }
        } catch (Exception e) {
            logger.error("Failed to update deck {} in cache for user {}: {}", deckId, userId, e.getMessage());
            throw new CacheOperationException("Failed to update deck in cache for user: " + userId, e);
        }
    }

    /**
     * Removes a single deck from the user's cache.
     * 
     * @param userId the UUID of the user whose deck cache to update
     * @param deckId the UUID of the deck to remove from cache
     * @return the updated cached decks, or null if no cache exists
     * @throws InvalidSessionParameterException if userId or deckId is null
     * @throws CacheOperationException if Redis operation fails
     */
    @SuppressWarnings("unused")
    public CachedDecks removeSingleDeckFromCache(@NonNull UUID userId, @NonNull UUID deckId) {
        if (userId == null) {
            throw new InvalidSessionParameterException("User ID cannot be null when removing deck from cache");
        }
        
        if (deckId == null) {
            throw new InvalidSessionParameterException("Deck ID cannot be null when removing deck from cache");
        }
        
        try {
            logger.debug("Removing deck {} from cache for user {}", deckId, userId);
            Optional<CachedDecks> existingCache = cachedDecksRepository.findById(userId);
            
            if (existingCache.isPresent()) {
                CachedDecks cachedDecks = existingCache.get();
                cachedDecks.removeDeck(deckId);
                CachedDecks savedCache = cachedDecksRepository.save(cachedDecks);
                logger.info("Successfully removed deck {} from cache for user {}", deckId, userId);
                return savedCache;
            }
            
            logger.debug("No cache found for user {} when trying to remove deck {}", userId, deckId);
            return null;
        } catch (Exception e) {
            logger.error("Failed to remove deck {} from cache for user {}: {}", deckId, userId, e.getMessage());
            throw new CacheOperationException("Failed to remove deck from cache for user: " + userId, e);
        }
    }

    /**
     * Refreshes cached decks for a user by fetching fresh data.
     * Useful when user creates/deletes decks and cache needs to be updated.
     * 
     * @param userId the UUID of the user whose deck cache to refresh
     * @param deckCaches the updated list of deck cache objects
     * @return the refreshed cached decks, or creates new cache if none exists
     * @throws InvalidSessionParameterException if userId is null
     * @throws CacheOperationException if Redis operation fails
     */
    @SuppressWarnings("unused")
    public CachedDecks refreshCachedDecks(@NonNull UUID userId, List<DeckCache> deckCaches) {
        if (userId == null) {
            throw new InvalidSessionParameterException("User ID cannot be null when refreshing deck cache");
        }
        
        try {
            logger.debug("Refreshing cached decks for user {}", userId);
            cachedDecksRepository.deleteById(userId);
            
            CachedDecks cachedDecks = new CachedDecks(userId, deckCaches);
            CachedDecks savedCache = cachedDecksRepository.save(cachedDecks);
            logger.info("Successfully refreshed cached decks for user {}", userId);
            return savedCache;
        } catch (Exception e) {
            logger.error("Failed to refresh cached decks for user {}: {}", userId, e.getMessage());
            throw new CacheOperationException("Failed to refresh deck cache for user: " + userId, e);
        }
    }

    /**
     * Invalidates (deletes) cached decks for a user.
     * Useful when user makes significant changes and cache should be rebuilt on next access.
     * 
     * @param userId the UUID of the user whose deck cache to invalidate
     * @throws InvalidSessionParameterException if userId is null
     * @throws CacheOperationException if Redis operation fails
     */
    @SuppressWarnings("unused")
    public void invalidateCachedDecks(@NonNull UUID userId) {
        if (userId == null) {
            throw new InvalidSessionParameterException("User ID cannot be null when invalidating deck cache");
        }
        
        try {
            logger.debug("Invalidating cached decks for user {}", userId);
            cachedDecksRepository.deleteById(userId);
            logger.info("Successfully invalidated cached decks for user {}", userId);
        } catch (Exception e) {
            logger.error("Failed to invalidate cached decks for user {}: {}", userId, e.getMessage());
            throw new CacheOperationException("Failed to invalidate deck cache for user: " + userId, e);
        }
    }

    /**
     * Refreshes a user's session by extending the expiration time.
     * Only works if the session is currently valid.
     * Updates the last accessed time and extends session expiration.
     * 
     * @param userId the UUID of the user whose session to refresh
     * @param additionalSeconds the number of seconds to extend the session
     * @return true if session was refreshed, false if no valid session exists
     * @throws InvalidSessionParameterException if userId is null or additionalSeconds is invalid
     * @throws SessionOperationException if Redis operation fails
     */
    @SuppressWarnings("unused")
    public boolean refreshSession(@NonNull UUID userId, long additionalSeconds) {
        if (userId == null) {
            throw new InvalidSessionParameterException("User ID cannot be null when refreshing session");
        }
        
        if (additionalSeconds <= 0) {
            throw new InvalidSessionParameterException("Additional seconds must be positive, got: " + additionalSeconds);
        }
        
        try {
            logger.debug("Refreshing session for user {} with {} additional seconds", userId, additionalSeconds);
            Optional<Session> sessionOpt = sessionRepository.findByUserId(userId);
            
            if (sessionOpt.isEmpty() || sessionOpt.get().isExpired()) {
                logger.debug("No valid session found for user {} to refresh", userId);
                return false;
            }
            
            Session session = sessionOpt.get();
            session.setLastAccessedAt();
            session.extendSession(additionalSeconds);
            sessionRepository.save(session);
            logger.info("Successfully refreshed session for user {}", userId);
            return true;
        } catch (Exception e) {
            logger.error("Failed to refresh session for user {}: {}", userId, e.getMessage());
            throw new SessionOperationException("Failed to refresh session for user: " + userId, e);
        }
    }

    /**
     * Refreshes a user's session with default 24-hour extension.
     * Only works if the session is currently valid.
     * Updates the last accessed time and extends session expiration.
     * 
     * @param userId the UUID of the user whose session to refresh
     * @return true if session was refreshed, false if no valid session exists
     * @throws InvalidSessionParameterException if userId is null
     * @throws SessionOperationException if Redis operation fails
     */
    @SuppressWarnings("unused")
    public boolean refreshSession(@NonNull UUID userId) {
        if (userId == null) {
            throw new InvalidSessionParameterException("User ID cannot be null when refreshing session");
        }
        
        try {
            logger.debug("Refreshing session for user {} with default extension", userId);
            Optional<Session> sessionOpt = sessionRepository.findByUserId(userId);
            
            if (sessionOpt.isEmpty() || sessionOpt.get().isExpired()) {
                logger.debug("No valid session found for user {} to refresh", userId);
                return false;
            }
            
            Session session = sessionOpt.get();
            session.setLastAccessedAt();
            session.extendSession();
            sessionRepository.save(session);
            logger.info("Successfully refreshed session for user {} with default extension", userId);
            return true;
        } catch (Exception e) {
            logger.error("Failed to refresh session for user {}: {}", userId, e.getMessage());
            throw new SessionOperationException("Failed to refresh session for user: " + userId, e);
        }
    }

    /**
     * Logs out a user by removing their session and clearing their cached data.
     * Performs complete cleanup of both Redis session and cache stores.
     * 
     * @param userId the UUID of the user to log out
     * @throws InvalidSessionParameterException if userId is null
     * @throws SessionOperationException if Redis operation fails
     */
    @SuppressWarnings("unused")
    public void logout(@NonNull UUID userId) {
        if (userId == null) {
            throw new InvalidSessionParameterException("User ID cannot be null when logging out");
        }
        
        try {
            logger.debug("Logging out user {}", userId);
            sessionRepository.deleteByUserId(userId);
            cachedDecksRepository.deleteById(userId);
            logger.info("Successfully logged out user {} and cleared cache", userId);
        } catch (Exception e) {
            logger.error("Failed to logout user {}: {}", userId, e.getMessage());
            throw new SessionOperationException("Failed to logout user: " + userId, e);
        }
    }
    
    /**
     * Validates a user's session and performs cleanup if session is invalid or expired.
     * Automatically removes orphaned cache data when session is not found or expired.
     * 
     * @param userId the UUID of the user whose session to validate
     * @return true if user has valid, non-expired session; false otherwise
     * @throws InvalidSessionParameterException if userId is null
     * @throws SessionOperationException if Redis operation fails
     */
    @SuppressWarnings("unused")
    public boolean validateSessionAndCleanupCache(@NonNull UUID userId) {
        if (userId == null) {
            throw new InvalidSessionParameterException("User ID cannot be null when validating session");
        }
        
        try {
            logger.debug("Validating session and cleaning up cache for user {}", userId);
            Optional<Session> session = sessionRepository.findByUserId(userId);
            
            if (session.isEmpty()) {
                logger.debug("No session found for user {}, cleaning up cache", userId);
                cachedDecksRepository.deleteById(userId);
                return false;
            } else if (session.get().isExpired()) {
                logger.debug("Session expired for user {}, cleaning up session and cache", userId);
                sessionRepository.deleteByUserId(userId);
                cachedDecksRepository.deleteById(userId);
                return false;
            }
            
            logger.debug("Valid session found for user {}", userId);
            return true;
        } catch (Exception e) {
            logger.error("Failed to validate session for user {}: {}", userId, e.getMessage());
            throw new SessionOperationException("Failed to validate session for user: " + userId, e);
        }
    }
    
    /**
     * Retrieves a user's cached decks only if they have a valid session.
     * Performs session validation first and returns empty if session is invalid.
     * This ensures cached data is only accessible with valid authentication.
     * 
     * @param userId the UUID of the user whose cached decks to retrieve
     * @return Optional containing cached decks if session is valid, empty otherwise
     * @throws InvalidSessionParameterException if userId is null
     * @throws CacheOperationException if Redis operation fails
     */
    @SuppressWarnings("unused")
    public Optional<CachedDecks> getCachedDecksIfSessionValid(@NonNull UUID userId) {
        if (userId == null) {
            throw new InvalidSessionParameterException("User ID cannot be null when retrieving cached decks");
        }
        
        try {
            logger.debug("Retrieving cached decks for user {} with session validation", userId);
            if (!validateSessionAndCleanupCache(userId)) {
                logger.debug("Session validation failed for user {}, returning empty", userId);
                return Optional.empty();
            }
            
            Optional<CachedDecks> cachedDecks = cachedDecksRepository.findById(userId);
            if (cachedDecks.isPresent()) {
                logger.debug("Found cached decks for user {}", userId);
            } else {
                logger.debug("No cached decks found for user {}", userId);
            }
            return cachedDecks;
        } catch (Exception e) {
            logger.error("Failed to retrieve cached decks for user {}: {}", userId, e.getMessage());
            throw new CacheOperationException("Failed to retrieve cached decks for user: " + userId, e);
        }
    }
}