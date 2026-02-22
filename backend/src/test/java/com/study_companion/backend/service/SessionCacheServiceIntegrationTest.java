package com.study_companion.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import com.study_companion.backend.exception.session.InvalidSessionParameterException;
import com.study_companion.backend.model.redis.CachedDecks;
import com.study_companion.backend.model.redis.DeckCache;
import com.study_companion.backend.model.redis.Session;

@SpringBootTest
@Transactional
@Rollback
public class SessionCacheServiceIntegrationTest {

    @Autowired
    private SessionCacheService sessionCacheService;
 
    @Test
    void createSession_ValidInput_ReturnsSession() {
        UUID userId = UUID.randomUUID();
        Long ttlSeconds = 3600L;

        Session session = sessionCacheService.createSession(userId, ttlSeconds);

        assertThat(session).isNotNull();
        assertThat(session.getSessionId()).isNotNull();
        assertThat(session.getUserId()).isEqualTo(userId);
        assertThat(session.getCreatedAt()).isNotNull();
        assertThat(session.getLastAccessedAt()).isNotNull();
        assertThat(session.getExpiresAt()).isNotNull();
    }

    @Test
    void createSession_ValidInputWithoutTTL_ReturnsSession() {
        UUID userId = UUID.randomUUID();

        Session session = sessionCacheService.createSession(userId, null);

        assertThat(session).isNotNull();
        assertThat(session.getSessionId()).isNotNull();
        assertThat(session.getUserId()).isEqualTo(userId);
        assertThat(session.getCreatedAt()).isNotNull();
        assertThat(session.getLastAccessedAt()).isNotNull();
        assertThat(session.getExpiresAt()).isNotNull();
    }

    @Test
    void createSession_NullUserId_ThrowsInvalidSessionParameterException() {
        InvalidSessionParameterException exception = assertThrows(InvalidSessionParameterException.class, () -> {
            sessionCacheService.createSession(null, 3600L);
        });

        assertThat(exception.getMessage()).isEqualTo("User ID cannot be null when creating a session");
    }

    @Test
    void createSession_NegativeTTL_ThrowsInvalidSessionParameterException() {
        UUID userId = UUID.randomUUID();

        InvalidSessionParameterException exception = assertThrows(InvalidSessionParameterException.class, () -> {
            sessionCacheService.createSession(userId, -100L);
        });

        assertThat(exception.getMessage()).contains("TTL must be positive when specified, got: -100");
    }

    @Test
    void createSession_ZeroTTL_ThrowsInvalidSessionParameterException() {
        UUID userId = UUID.randomUUID();

        InvalidSessionParameterException exception = assertThrows(InvalidSessionParameterException.class, () -> {
            sessionCacheService.createSession(userId, 0L);
        });

        assertThat(exception.getMessage()).contains("TTL must be positive when specified, got: 0");
    }

    @Test
    void createOrUpdateCachedDecks_ValidInput_ReturnsCachedDecks() {
        UUID userId = UUID.randomUUID();
        List<DeckCache> deckCaches = new ArrayList<>();
        DeckCache deckCache = new DeckCache(UUID.randomUUID(), "Test Deck", "Test Description");
        deckCaches.add(deckCache);

        CachedDecks cachedDecks = sessionCacheService.createOrUpdateCachedDecks(userId, deckCaches);

        assertThat(cachedDecks).isNotNull();
        assertThat(cachedDecks.getUserId()).isEqualTo(userId);
        assertThat(cachedDecks.getDecks()).hasSize(1);
        assertThat(cachedDecks.getDecks().get(0).getTitle()).isEqualTo("Test Deck");
    }

    @Test 
    void createOrUpdateCachedDecks_EmptyDeckList_ReturnsCachedDecks() {
        UUID userId = UUID.randomUUID();
        List<DeckCache> deckCaches = new ArrayList<>();

        CachedDecks cachedDecks = sessionCacheService.createOrUpdateCachedDecks(userId, deckCaches);

        assertThat(cachedDecks).isNotNull();
        assertThat(cachedDecks.getUserId()).isEqualTo(userId);
        assertThat(cachedDecks.getDecks()).isEmpty();
    }

    @Test 
    void createOrUpdateCachedDecks_NullDeckList_ReturnsCachedDecks() {
        UUID userId = UUID.randomUUID();

        CachedDecks cachedDecks = sessionCacheService.createOrUpdateCachedDecks(userId, null);

        assertThat(cachedDecks).isNotNull();
        assertThat(cachedDecks.getUserId()).isEqualTo(userId);
        assertThat(cachedDecks.getDecks()).isEmpty();
    }

    @Test 
    void createOrUpdateCachedDecks_NullUserId_ThrowsInvalidSessionParameterException() {
        List<DeckCache> deckCaches = new ArrayList<>();

        InvalidSessionParameterException exception = assertThrows(InvalidSessionParameterException.class, () -> {
            sessionCacheService.createOrUpdateCachedDecks(null, deckCaches);
        });

        assertThat(exception.getMessage()).isEqualTo("User ID cannot be null when caching decks");
    }

    @Test 
    void createOrUpdateCachedDecks_UpdateExisting_ReturnsUpdatedCachedDecks() {
        UUID userId = UUID.randomUUID();
        List<DeckCache> initialDecks = new ArrayList<>();
        DeckCache deckCache1 = new DeckCache(UUID.randomUUID(), "Initial Deck", "Initial Description");
        initialDecks.add(deckCache1);

        sessionCacheService.createOrUpdateCachedDecks(userId, initialDecks);

        List<DeckCache> updatedDecks = new ArrayList<>();
        DeckCache deckCache2 = new DeckCache(UUID.randomUUID(), "Updated Deck", "Updated Description");
        updatedDecks.add(deckCache2);

        CachedDecks updatedCachedDecks = sessionCacheService.createOrUpdateCachedDecks(userId, updatedDecks);

        assertThat(updatedCachedDecks).isNotNull();
        assertThat(updatedCachedDecks.getDecks()).hasSize(1);
        assertThat(updatedCachedDecks.getDecks().get(0).getTitle()).isEqualTo("Updated Deck");
    }

    @Test 
    void updateSpecificDeckInCache_ValidInput_ReturnsUpdatedCachedDecks() {
        UUID userId = UUID.randomUUID();
        UUID deckId = UUID.randomUUID();
        DeckCache updatedDeckCache = new DeckCache(deckId, "Updated Deck", "Updated Description");

        CachedDecks cachedDecks = sessionCacheService.updateSpecificDeckInCache(userId, deckId, updatedDeckCache);

        assertThat(cachedDecks).isNotNull();
        assertThat(cachedDecks.getUserId()).isEqualTo(userId);
        assertThat(cachedDecks.getDecks()).hasSize(1);
        assertThat(cachedDecks.getDecks().get(0).getTitle()).isEqualTo("Updated Deck");
    }

    @Test 
    void updateSpecificDeckInCache_NullUserId_ThrowsInvalidSessionParameterException() {
        UUID deckId = UUID.randomUUID();
        DeckCache updatedDeckCache = new DeckCache(UUID.randomUUID(), "Test", "Test Description");

        InvalidSessionParameterException exception = assertThrows(InvalidSessionParameterException.class, () -> {
            sessionCacheService.updateSpecificDeckInCache(null, deckId, updatedDeckCache);
        });

        assertThat(exception.getMessage()).isEqualTo("User ID cannot be null when updating deck cache");
    }

    @Test 
    void updateSpecificDeckInCache_NullDeckId_ThrowsInvalidSessionParameterException() {
        UUID userId = UUID.randomUUID();
        DeckCache updatedDeckCache = new DeckCache(UUID.randomUUID(), "Test", "Test Description");

        InvalidSessionParameterException exception = assertThrows(InvalidSessionParameterException.class, () -> {
            sessionCacheService.updateSpecificDeckInCache(userId, null, updatedDeckCache);
        });

        assertThat(exception.getMessage()).isEqualTo("Deck ID cannot be null when updating deck cache");
    }

    @Test 
    void updateSpecificDeckInCache_NullDeckCache_ThrowsInvalidSessionParameterException() {
        UUID userId = UUID.randomUUID();
        UUID deckId = UUID.randomUUID();

        InvalidSessionParameterException exception = assertThrows(InvalidSessionParameterException.class, () -> {
            sessionCacheService.updateSpecificDeckInCache(userId, deckId, null);
        });

        assertThat(exception.getMessage()).isEqualTo("Updated deck cache cannot be null");
    }

    @Test 
    void removeSingleDeckFromCache_ValidInput_ReturnsUpdatedCachedDecks() {
        UUID userId = UUID.randomUUID();
        UUID deckId = UUID.randomUUID();
        
        List<DeckCache> deckCaches = new ArrayList<>();
        DeckCache deckCache = new DeckCache(deckId, "Test Deck", "Test Description");
        deckCaches.add(deckCache);
        sessionCacheService.createOrUpdateCachedDecks(userId, deckCaches);

        CachedDecks updatedCache = sessionCacheService.removeSingleDeckFromCache(userId, deckId);

        assertThat(updatedCache).isNotNull();
        assertThat(updatedCache.getDecks()).isEmpty();
    }

    @Test 
    void removeSingleDeckFromCache_NoExistingCache_ReturnsNull() {
        UUID userId = UUID.randomUUID();
        UUID deckId = UUID.randomUUID();

        CachedDecks result = sessionCacheService.removeSingleDeckFromCache(userId, deckId);

        assertThat(result).isNull();
    }

    @Test 
    void removeSingleDeckFromCache_NullUserId_ThrowsInvalidSessionParameterException() {
        UUID deckId = UUID.randomUUID();

        InvalidSessionParameterException exception = assertThrows(InvalidSessionParameterException.class, () -> {
            sessionCacheService.removeSingleDeckFromCache(null, deckId);
        });

        assertThat(exception.getMessage()).isEqualTo("User ID cannot be null when removing deck from cache");
    }

    @Test 
    void removeSingleDeckFromCache_NullDeckId_ThrowsInvalidSessionParameterException() {
        UUID userId = UUID.randomUUID();

        InvalidSessionParameterException exception = assertThrows(InvalidSessionParameterException.class, () -> {
            sessionCacheService.removeSingleDeckFromCache(userId, null);
        });

        assertThat(exception.getMessage()).isEqualTo("Deck ID cannot be null when removing deck from cache");
    }

    @Test 
    void refreshCachedDecks_ValidInput_ReturnsCachedDecks() {
        UUID userId = UUID.randomUUID();
        List<DeckCache> deckCaches = new ArrayList<>();
        DeckCache deckCache = new DeckCache(UUID.randomUUID(), "Refreshed Deck", "Refreshed Description");
        deckCaches.add(deckCache);

        CachedDecks cachedDecks = sessionCacheService.refreshCachedDecks(userId, deckCaches);

        assertThat(cachedDecks).isNotNull();
        assertThat(cachedDecks.getUserId()).isEqualTo(userId);
        assertThat(cachedDecks.getDecks()).hasSize(1);
        assertThat(cachedDecks.getDecks().get(0).getTitle()).isEqualTo("Refreshed Deck");
    }

    @Test 
    void refreshCachedDecks_NullUserId_ThrowsInvalidSessionParameterException() {
        List<DeckCache> deckCaches = new ArrayList<>();

        InvalidSessionParameterException exception = assertThrows(InvalidSessionParameterException.class, () -> {
            sessionCacheService.refreshCachedDecks(null, deckCaches);
        });

        assertThat(exception.getMessage()).isEqualTo("User ID cannot be null when refreshing deck cache");
    }

    @Test 
    void invalidateCachedDecks_ValidInput_DoesNotThrow() {
        UUID userId = UUID.randomUUID();
        
        sessionCacheService.createOrUpdateCachedDecks(userId, new ArrayList<>());

        sessionCacheService.invalidateCachedDecks(userId);
    }

    @Test 
    void invalidateCachedDecks_NullUserId_ThrowsInvalidSessionParameterException() {
        InvalidSessionParameterException exception = assertThrows(InvalidSessionParameterException.class, () -> {
            sessionCacheService.invalidateCachedDecks(null);
        });

        assertThat(exception.getMessage()).isEqualTo("User ID cannot be null when invalidating deck cache");
    }

    @Test 
    void refreshSession_ValidInput_ReturnsTrue() {
        UUID userId = UUID.randomUUID();
        
        sessionCacheService.createSession(userId, 3600L);

        boolean result = sessionCacheService.refreshSession(userId, 1800L);

        assertThat(result).isTrue();
    }

    @Test 
    void refreshSession_NoExistingSession_ReturnsFalse() {
        UUID userId = UUID.randomUUID();

        boolean result = sessionCacheService.refreshSession(userId, 1800L);

        assertThat(result).isFalse();
    }

    @Test 
    void refreshSession_NullUserId_ThrowsInvalidSessionParameterException() {
        InvalidSessionParameterException exception = assertThrows(InvalidSessionParameterException.class, () -> {
            sessionCacheService.refreshSession(null, 1800L);
        });

        assertThat(exception.getMessage()).isEqualTo("User ID cannot be null when refreshing session");
    }

    @Test 
    void refreshSession_NegativeAdditionalSeconds_ThrowsInvalidSessionParameterException() {
        UUID userId = UUID.randomUUID();

        InvalidSessionParameterException exception = assertThrows(InvalidSessionParameterException.class, () -> {
            sessionCacheService.refreshSession(userId, -100L);
        });

        assertThat(exception.getMessage()).contains("Additional seconds must be positive, got: -100");
    }

    @Test 
    void refreshSession_ZeroAdditionalSeconds_ThrowsInvalidSessionParameterException() {
        UUID userId = UUID.randomUUID();

        InvalidSessionParameterException exception = assertThrows(InvalidSessionParameterException.class, () -> {
            sessionCacheService.refreshSession(userId, 0L);
        });

        assertThat(exception.getMessage()).contains("Additional seconds must be positive, got: 0");
    }

    @Test 
    void refreshSessionDefault_ValidInput_ReturnsTrue() {
        UUID userId = UUID.randomUUID();
        
        sessionCacheService.createSession(userId, 3600L);

        boolean result = sessionCacheService.refreshSession(userId);

        assertThat(result).isTrue();
    }

    @Test 
    void refreshSessionDefault_NoExistingSession_ReturnsFalse() {
        UUID userId = UUID.randomUUID();

        boolean result = sessionCacheService.refreshSession(userId);

        assertThat(result).isFalse();
    }

    @Test 
    void refreshSessionDefault_NullUserId_ThrowsInvalidSessionParameterException() {
        InvalidSessionParameterException exception = assertThrows(InvalidSessionParameterException.class, () -> {
            sessionCacheService.refreshSession(null);
        });

        assertThat(exception.getMessage()).isEqualTo("User ID cannot be null when refreshing session");
    }

    @Test 
    void logout_ValidInput_DoesNotThrow() {
        UUID userId = UUID.randomUUID();
        
        sessionCacheService.createSession(userId, 3600L);
        sessionCacheService.createOrUpdateCachedDecks(userId, new ArrayList<>());

        sessionCacheService.clearSessionAndCachedDecks(userId);
    }

    @Test 
    void logout_NullUserId_ThrowsInvalidSessionParameterException() {
        InvalidSessionParameterException exception = assertThrows(InvalidSessionParameterException.class, () -> {
            sessionCacheService.clearSessionAndCachedDecks(null);
        });

        assertThat(exception.getMessage()).isEqualTo("User ID cannot be null when logging out");
    }

    @Test 
    void getCachedDecksIfSessionValid_ValidSession_ReturnsCachedDecks() {
        UUID userId = UUID.randomUUID();
        
        sessionCacheService.createSession(userId, 3600L);
        List<DeckCache> deckCaches = new ArrayList<>();
        DeckCache deckCache = new DeckCache(UUID.randomUUID(), "Test Deck", "Test Description");
        deckCaches.add(deckCache);
        sessionCacheService.createOrUpdateCachedDecks(userId, deckCaches);

        Optional<CachedDecks> result = sessionCacheService.getCachedDecksIfSessionValid(userId);

        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(userId);
        assertThat(result.get().getDecks()).hasSize(1);
    }

    @Test 
    void getCachedDecksIfSessionValid_NoSession_ReturnsEmpty() {
        UUID userId = UUID.randomUUID();

        Optional<CachedDecks> result = sessionCacheService.getCachedDecksIfSessionValid(userId);

        assertThat(result).isEmpty();
    }

    @Test 
    void getCachedDecksIfSessionValid_SessionButNoCache_ReturnsEmpty() {
        UUID userId = UUID.randomUUID();
        
        sessionCacheService.createSession(userId, 3600L);

        Optional<CachedDecks> result = sessionCacheService.getCachedDecksIfSessionValid(userId);

        assertThat(result).isEmpty();
    }

    @Test 
    void getCachedDecksIfSessionValid_NullUserId_ThrowsInvalidSessionParameterException() {
        InvalidSessionParameterException exception = assertThrows(InvalidSessionParameterException.class, () -> {
            sessionCacheService.getCachedDecksIfSessionValid(null);
        });

        assertThat(exception.getMessage()).isEqualTo("User ID cannot be null when retrieving cached decks");
    }
}