package com.study_companion.backend.model.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.UUID;

public class CachedDecksTest {

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    void testDefaultConstructor() {
        CachedDecks cachedDecks = new CachedDecks();
        assertNotNull(cachedDecks);
        assertNull(cachedDecks.getUserId());
        assertNotNull(cachedDecks.getDecks());
        assertTrue(cachedDecks.getDecks().isEmpty());
        assertNotNull(cachedDecks.getCachedAt());
        assertNotNull(cachedDecks.getLastModified());
    }

    @Test
    void testParameterizedConstructorWithUserId() {
        CachedDecks cachedDecks = new CachedDecks(userId);
        assertNotNull(cachedDecks);
        assertEquals(userId, cachedDecks.getUserId());
        assertNotNull(cachedDecks.getDecks());
        assertTrue(cachedDecks.getDecks().isEmpty());
        assertNotNull(cachedDecks.getCachedAt());
        assertNotNull(cachedDecks.getLastModified());
    }

    @Test
    void testParameterizedConstructorWithUserIdAndDecks() {
        UUID deckId1 = UUID.randomUUID();
        UUID deckId2 = UUID.randomUUID();

        DeckCache deck1 = new DeckCache(deckId1, "Math Basics", "Algebra fundamentals", 15);
        DeckCache deck2 = new DeckCache(deckId2, "History", "World War II", 8);

        CachedDecks cachedDecks = new CachedDecks(userId, Arrays.asList(deck1, deck2));
        assertNotNull(cachedDecks);
        assertEquals(userId, cachedDecks.getUserId());
        assertNotNull(cachedDecks.getDecks());
        assertEquals(2, cachedDecks.getDecks().size());
        assertNotNull(cachedDecks.getCachedAt());
        assertNotNull(cachedDecks.getLastModified());
    }

    @Test
    void testAddDeck() {
        CachedDecks cachedDecks = new CachedDecks(userId);

        UUID deckId = UUID.randomUUID();
        DeckCache deck = new DeckCache(deckId, "Science", "Basic Physics", 12);

        cachedDecks.addDeck(deck);

        assertEquals(1, cachedDecks.getDecks().size());
        assertEquals(deck, cachedDecks.getDecks().get(0));
    }

    @Test
    void testRemoveDeck() {
        UUID deckId1 = UUID.randomUUID();
        UUID deckId2 = UUID.randomUUID();

        DeckCache deck1 = new DeckCache(deckId1, "Math Basics", "Algebra fundamentals", 15);
        DeckCache deck2 = new DeckCache(deckId2, "History", "World War II", 8);

        CachedDecks cachedDecks = new CachedDecks(userId, Arrays.asList(deck1, deck2));

        cachedDecks.removeDeck(deckId1);

        assertEquals(1, cachedDecks.getDecks().size());
        assertEquals(deck2, cachedDecks.getDecks().get(0));
    }

    @Test
    void testFindDeckById() {
        UUID deckId1 = UUID.randomUUID();
        UUID deckId2 = UUID.randomUUID();

        DeckCache deck1 = new DeckCache(deckId1, "Math Basics", "Algebra fundamentals", 15);
        DeckCache deck2 = new DeckCache(deckId2, "History", "World War II", 8);

        CachedDecks cachedDecks = new CachedDecks(userId, Arrays.asList(deck1, deck2));

        DeckCache foundDeck = cachedDecks.findDeckById(deckId2);

        assertNotNull(foundDeck);
        assertEquals(deck2, foundDeck);
    }

    @Test
    void testUpdateDeck() {
        UUID deckId = UUID.randomUUID();

        DeckCache originalDeck = new DeckCache(deckId, "Math Basics", "Algebra fundamentals", 15);

        CachedDecks cachedDecks = new CachedDecks(userId, Arrays.asList(originalDeck));

        DeckCache updatedDeck = new DeckCache(deckId, "Math Basics Updated", "Advanced Algebra", 20);

        cachedDecks.updateDeck(updatedDeck);

        DeckCache foundDeck = cachedDecks.findDeckById(deckId);

        assertNotNull(foundDeck);
        assertEquals("Math Basics Updated", foundDeck.getTitle());
        assertEquals("Advanced Algebra", foundDeck.getDescription());
        assertEquals(20, foundDeck.getCardCount());
    }

    @Test
    void testGetDeckCount() {
        UUID deckId1 = UUID.randomUUID();
        UUID deckId2 = UUID.randomUUID();

        DeckCache deck1 = new DeckCache(deckId1, "Math Basics", "Algebra fundamentals", 15);
        DeckCache deck2 = new DeckCache(deckId2, "History", "World War II", 8);

        CachedDecks cachedDecks = new CachedDecks(userId, Arrays.asList(deck1, deck2));

        assertEquals(2, cachedDecks.getDecks().size());
    }

    @Test
    void testIsEmpty() {
        CachedDecks cachedDecks = new CachedDecks(userId);
        assertTrue(cachedDecks.getDecks().isEmpty());

        UUID deckId = UUID.randomUUID();
        DeckCache deck = new DeckCache(deckId, "Science", "Basic Physics", 12);
        cachedDecks.addDeck(deck);

        assertFalse(cachedDecks.getDecks().isEmpty());
    }

    @Test
    void testToString() {
        CachedDecks cachedDecks = new CachedDecks(userId);

        String expectedString = "CachedDecks{" +
                "userId='" + userId + '\'' +
                ", deckCount=" + cachedDecks.getDeckCount() +
                ", cachedAt=" + cachedDecks.getCachedAt() +
                ", lastModified=" + cachedDecks.getLastModified() +
                '}';
        assertEquals(expectedString, cachedDecks.toString());
    }
}
