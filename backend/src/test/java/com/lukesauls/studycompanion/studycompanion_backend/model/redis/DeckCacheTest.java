package com.lukesauls.studycompanion.studycompanion_backend.model.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import java.time.LocalDateTime;

public class DeckCacheTest {
    
    private UUID deckId;

    @BeforeEach
    void setUp() {
        deckId = UUID.randomUUID();
    }

    @Test
    void testDefaultConstructor() {
        DeckCache deckCache = new DeckCache();
        assertNotNull(deckCache);
        assertNull(deckCache.getDeckId());
        assertNull(deckCache.getTitle());
        assertNull(deckCache.getDescription());
        assertEquals(0, deckCache.getCardCount());
        assertNotNull(deckCache.getCreatedAt());
        assertNotNull(deckCache.getUpdatedAt());
    }
}
