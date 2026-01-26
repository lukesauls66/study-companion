package com.study_companion.backend.model.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

public class DeckCacheTest {

    private UUID deckId;
    private String title = "Sample Deck";
    private String description = "This is a sample deck description.";
    private int cardCount = 10;

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

    @Test
    void testParameterizedConstructorWithoutCardCount() {
        DeckCache deckCache = new DeckCache(deckId, title, description);
        assertNotNull(deckCache);
        assertEquals(deckId, deckCache.getDeckId());
        assertEquals(title, deckCache.getTitle());
        assertEquals(description, deckCache.getDescription());
        assertEquals(0, deckCache.getCardCount());
        assertNotNull(deckCache.getCreatedAt());
        assertNotNull(deckCache.getUpdatedAt());
    }

    @Test
    void testParameterizedConstructorWithCardCount() {
        DeckCache deckCache = new DeckCache(deckId, title, description, cardCount);
        assertNotNull(deckCache);
        assertEquals(deckId, deckCache.getDeckId());
        assertEquals(title, deckCache.getTitle());
        assertEquals(description, deckCache.getDescription());
        assertEquals(cardCount, deckCache.getCardCount());
        assertNotNull(deckCache.getCreatedAt());
        assertNotNull(deckCache.getUpdatedAt());
    }

    @Test
    void testGettersAndSetters() {
        DeckCache deckCache = new DeckCache();

        deckCache.setCardCount(cardCount);
        deckCache.setUpdatedAt();

        assertEquals(cardCount, deckCache.getCardCount());
        assertTrue(deckCache.getUpdatedAt().isAfter(deckCache.getCreatedAt()) ||
                deckCache.getUpdatedAt().isEqual(deckCache.getCreatedAt()));
    }

    @Test
    void testToString() {
        DeckCache deckCache = new DeckCache(deckId, title, description, cardCount);
        String expectedString = "DeckCache{" +
                "deckId='" + deckId + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", cardCount=" + cardCount +
                ", createdAt=" + deckCache.getCreatedAt() +
                ", updatedAt=" + deckCache.getUpdatedAt() +
                '}';
        assertEquals(expectedString, deckCache.toString());
    }
}
