package com.study_companion.backend.model.mongo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

public class ReviewSessionTest {

    private UUID deckId;
    private UUID userId;
    private String deckName;
    private int score;
    private int cardsReviewed;
    private int correctAnswers;

    @BeforeEach
    void setUp() {
        deckId = UUID.randomUUID();
        userId = UUID.randomUUID();
        deckName = "Sample Deck";
        score = 85;
        cardsReviewed = 20;
        correctAnswers = 17;
    }

    @Test
    void testDefaultConstructor() {
        ReviewSession session = new ReviewSession();
        assertNotNull(session);
        assertNotNull(session.getId());
        assertNotNull(session.getDate());
        assertNull(session.getDeckName());
        assertEquals(0, session.getScore());
        assertEquals(0, session.getCardsReviewed());
        assertEquals(0, session.getCorrectAnswers());
    }

    @Test
    void testParameterizedConstructor() {
        ReviewSession session = new ReviewSession(deckId, userId, deckName, score, cardsReviewed, correctAnswers);
        assertNotNull(session);
        assertNotNull(session.getId());
        assertNotNull(session.getDate());
        assertEquals(deckName, session.getDeckName());
        assertEquals(score, session.getScore());
        assertEquals(cardsReviewed, session.getCardsReviewed());
        assertEquals(correctAnswers, session.getCorrectAnswers());
    }
    
    @Test
    void testToString() {
        ReviewSession session = new ReviewSession(deckId, userId, deckName, score, cardsReviewed, correctAnswers);
        String expectedString = "ReviewSession{" +
                "id='" + session.getId() + '\'' +
                ", date=" + session.getDate() +
                ", deckName='" + deckName + '\'' +
                ", score=" + score +
                ", cardsReviewed=" + cardsReviewed +
                ", correctAnswers=" + correctAnswers +
                '}';
        assertEquals(expectedString, session.toString());
    }
}
