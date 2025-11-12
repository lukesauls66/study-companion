package com.lukesauls.studycompanion.studycompanion_backend.model.mongo;

import com.lukesauls.studycompanion.studycompanion_backend.model.CardCreationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

public class DeckAnalyticsTest {
    
    private UUID deckId;
    private UUID userId;
    
    @BeforeEach
    void setUp() {
        deckId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    @Test
    void testDefaultConstructor() {
        DeckAnalytics analytics = new DeckAnalytics();
        assertNotNull(analytics);
        assertNotNull(analytics.getId());
        assertEquals(0, analytics.getHighestScore());
        assertNull(analytics.getHighestScoreDate());
        assertEquals(0, analytics.getPreviousScore());
        assertNull(analytics.getPreviousScoreDate());
        assertNotNull(analytics.getReviewSessions());
        assertTrue(analytics.getReviewSessions().isEmpty());
        assertFalse(analytics.isProficiency());
    }

    @Test
    void testParameterizedConstructor() {
        DeckAnalytics analytics = new DeckAnalytics(deckId, userId);
        assertNotNull(analytics);
        assertNotNull(analytics.getId());
        assertEquals(deckId, analytics.getDeckId());
        assertEquals(userId, analytics.getUserId());
        assertEquals(0, analytics.getHighestScore());
        assertNull(analytics.getHighestScoreDate());
        assertEquals(0, analytics.getPreviousScore());
        assertNull(analytics.getPreviousScoreDate());
        assertNotNull(analytics.getReviewSessions());
        assertTrue(analytics.getReviewSessions().isEmpty());
        assertFalse(analytics.isProficiency());
    }

    @Test
    void testGettersAndSetters() {
        DeckAnalytics analytics = new DeckAnalytics();
        
        analytics.setDeckId(deckId);
        analytics.setUserId(userId);
        
        assertEquals(deckId, analytics.getDeckId());
        assertEquals(userId, analytics.getUserId());
    }

    @Test
    void testAddReviewSession() {
        DeckAnalytics analytics = new DeckAnalytics();
        ReviewSession session = new ReviewSession();
        analytics.addReviewSession(session);
        
        assertEquals(1, analytics.getReviewSessions().size());
        assertEquals(session, analytics.getReviewSessions().get(0));
    }

    @Test
    void testGetAverageScore() {
        DeckAnalytics analytics = new DeckAnalytics();
        
        ReviewSession session1 = new ReviewSession();
        session1.setScore(80);
        ReviewSession session2 = new ReviewSession();
        session2.setScore(90);
        
        analytics.addReviewSession(session1);
        analytics.addReviewSession(session2);
        
        assertEquals(85.0, analytics.getAverageScore());
    }
}
