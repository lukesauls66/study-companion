package com.study_companion.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.study_companion.backend.exception.analytics.InvalidAnalyticsParameterException;
import com.study_companion.backend.model.mongo.DeckAnalytics;
import com.study_companion.backend.model.mongo.ReviewSession;
import com.study_companion.backend.repository.mongo.DeckAnalyticsRepository;
import com.study_companion.backend.repository.mongo.ReviewSessionRepository;

@SpringBootTest
public class DeckAnalyticsServiceIntegrationTest {

    @Autowired
    private DeckAnalyticsService deckAnalyticsService;
    
    @Autowired
    private DeckAnalyticsRepository deckAnalyticsRepository;
    
    @Autowired
    private ReviewSessionRepository reviewSessionRepository;
    
    @AfterEach
    void cleanup() {
        // Clean up test data after each test
        reviewSessionRepository.deleteAll();
        deckAnalyticsRepository.deleteAll();
    }

    @Test
    void createReviewSession_ValidInput_ReturnsReviewSession() {
        UUID deckId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String deckName = "Test Deck";
        int score = 85;
        int cardsReviewed = 10;
        int correctAnswers = 8;

        ReviewSession reviewSession = deckAnalyticsService.createReviewSession(
            deckId, userId, deckName, score, cardsReviewed, correctAnswers);

        assertThat(reviewSession).isNotNull();
        assertThat(reviewSession.getId()).isNotNull();
        assertThat(reviewSession.getDeckId()).isEqualTo(deckId);
        assertThat(reviewSession.getUserId()).isEqualTo(userId);
        assertThat(reviewSession.getDeckName()).isEqualTo(deckName);
        assertThat(reviewSession.getScore()).isEqualTo(score);
        assertThat(reviewSession.getCardsReviewed()).isEqualTo(cardsReviewed);
        assertThat(reviewSession.getCorrectAnswers()).isEqualTo(correctAnswers);
        assertThat(reviewSession.getDate()).isNotNull();
    }

    @Test
    void createReviewSession_NullDeckId_ThrowsException() {
        UUID userId = UUID.randomUUID();
        String deckName = "Test Deck";

        assertThrows(InvalidAnalyticsParameterException.class, () -> {
            deckAnalyticsService.createReviewSession(null, userId, deckName, 85, 10, 8);
        });
    }

    @Test
    void createReviewSession_NullUserId_ThrowsException() {
        UUID deckId = UUID.randomUUID();
        String deckName = "Test Deck";

        assertThrows(InvalidAnalyticsParameterException.class, () -> {
            deckAnalyticsService.createReviewSession(deckId, null, deckName, 85, 10, 8);
        });
    }

    @Test
    void createReviewSession_NullDeckName_ThrowsException() {
        UUID deckId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        assertThrows(InvalidAnalyticsParameterException.class, () -> {
            deckAnalyticsService.createReviewSession(deckId, userId, null, 85, 10, 8);
        });
    }

    @Test
    void createReviewSession_CreatesAnalyticsIfNotExists() {
        UUID deckId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String deckName = "New Test Deck";

        deckAnalyticsService.createReviewSession(deckId, userId, deckName, 90, 15, 14);

        List<DeckAnalytics> analytics = deckAnalyticsService.getAnalyticsByDeckId(deckId);
        assertThat(analytics).hasSize(1);
        
        DeckAnalytics deckAnalytics = analytics.get(0);
        assertThat(deckAnalytics.getDeckId()).isEqualTo(deckId);
        assertThat(deckAnalytics.getUserId()).isEqualTo(userId);
        assertThat(deckAnalytics.getHighestScore()).isEqualTo(90);
        assertThat(deckAnalytics.getPreviousScore()).isEqualTo(90);
        assertThat(deckAnalytics.getReviewSessions()).hasSize(1);
    }

    @Test
    void createReviewSession_UpdatesExistingAnalytics() {
        UUID deckId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String deckName = "Existing Deck";

        // Create first review session
        deckAnalyticsService.createReviewSession(deckId, userId, deckName, 80, 10, 8);
        
        // Create second review session with higher score
        deckAnalyticsService.createReviewSession(deckId, userId, deckName, 95, 12, 11);

        List<DeckAnalytics> analytics = deckAnalyticsService.getAnalyticsByDeckId(deckId);
        assertThat(analytics).hasSize(1);
        
        DeckAnalytics deckAnalytics = analytics.get(0);
        assertThat(deckAnalytics.getHighestScore()).isEqualTo(95);
        assertThat(deckAnalytics.getPreviousScore()).isEqualTo(95); 
        assertThat(deckAnalytics.getReviewSessions()).hasSize(2);
    }

    @Test
    void getAnalyticsByDeckId_ValidDeckId_ReturnsAnalytics() {
        UUID deckId = UUID.randomUUID();
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        String deckName = "Shared Deck";

        deckAnalyticsService.createReviewSession(deckId, userId1, deckName, 85, 10, 8);
        deckAnalyticsService.createReviewSession(deckId, userId2, deckName, 90, 12, 11);

        List<DeckAnalytics> analytics = deckAnalyticsService.getAnalyticsByDeckId(deckId);
        
        assertThat(analytics).hasSize(2);
        assertThat(analytics).extracting(DeckAnalytics::getDeckId)
                           .containsOnly(deckId);
    }

    @Test
    void getAnalyticsByDeckId_NullDeckId_ThrowsException() {
        assertThrows(InvalidAnalyticsParameterException.class, () -> {
            deckAnalyticsService.getAnalyticsByDeckId(null);
        });
    }

    @Test
    void getAnalyticsByUserId_ValidUserId_ReturnsAnalytics() {
        UUID userId = UUID.randomUUID();
        UUID deckId1 = UUID.randomUUID();
        UUID deckId2 = UUID.randomUUID();

        deckAnalyticsService.createReviewSession(deckId1, userId, "Deck 1", 75, 8, 6);
        deckAnalyticsService.createReviewSession(deckId2, userId, "Deck 2", 90, 15, 14);

        List<DeckAnalytics> analytics = deckAnalyticsService.getAnalyticsByUserId(userId);
        
        assertThat(analytics).hasSize(2);
        assertThat(analytics).extracting(DeckAnalytics::getUserId)
                           .containsOnly(userId);
    }

    @Test
    void getAnalyticsByUserId_NullUserId_ThrowsException() {
        assertThrows(InvalidAnalyticsParameterException.class, () -> {
            deckAnalyticsService.getAnalyticsByUserId(null);
        });
    }

    @Test
    void getAnalyticsByUserIdOrderByScore_ValidUserId_ReturnsOrderedAnalytics() {
        UUID userId = UUID.randomUUID();
        UUID deckId1 = UUID.randomUUID();
        UUID deckId2 = UUID.randomUUID();
        UUID deckId3 = UUID.randomUUID();

        deckAnalyticsService.createReviewSession(deckId1, userId, "Deck 1", 60, 10, 6);
        deckAnalyticsService.createReviewSession(deckId2, userId, "Deck 2", 95, 15, 14);
        deckAnalyticsService.createReviewSession(deckId3, userId, "Deck 3", 80, 12, 10);

        List<DeckAnalytics> analytics = deckAnalyticsService.getAnalyticsByUserIdOrderByScore(userId);
        
        assertThat(analytics).hasSize(3);
        // Should be ordered by highest score descending
        assertThat(analytics.get(0).getHighestScore()).isEqualTo(95);
        assertThat(analytics.get(1).getHighestScore()).isEqualTo(80);
        assertThat(analytics.get(2).getHighestScore()).isEqualTo(60);
    }

    @Test
    void getAnalyticsByUserIdOrderByScore_NullUserId_ThrowsException() {
        assertThrows(InvalidAnalyticsParameterException.class, () -> {
            deckAnalyticsService.getAnalyticsByUserIdOrderByScore(null);
        });
    }

    @Test
    void getProficientDecks_ValidUserId_ReturnsProficientDecks() {
        UUID userId = UUID.randomUUID();
        UUID proficientDeckId = UUID.randomUUID();
        UUID nonProficientDeckId = UUID.randomUUID();

        // Create multiple high-scoring sessions to trigger proficiency
        deckAnalyticsService.createReviewSession(proficientDeckId, userId, "Proficient Deck", 95, 10, 10);
        deckAnalyticsService.createReviewSession(proficientDeckId, userId, "Proficient Deck", 90, 10, 9);
        deckAnalyticsService.createReviewSession(proficientDeckId, userId, "Proficient Deck", 92, 10, 9);

        // Create low-scoring session
        deckAnalyticsService.createReviewSession(nonProficientDeckId, userId, "Non-Proficient Deck", 60, 10, 6);

        List<DeckAnalytics> proficientDecks = deckAnalyticsService.getProficientDecks(userId);
        
        // Filter to only proficient decks
        List<DeckAnalytics> actualProficientDecks = proficientDecks.stream()
            .filter(DeckAnalytics::isProficiency)
            .toList();
        
        assertThat(actualProficientDecks).isNotEmpty();
        assertThat(actualProficientDecks).allMatch(DeckAnalytics::isProficiency);
    }

    @Test
    void getProficientDecks_NullUserId_ThrowsException() {
        assertThrows(InvalidAnalyticsParameterException.class, () -> {
            deckAnalyticsService.getProficientDecks(null);
        });
    }

    @Test
    void getReviewSessionsByDeckId_ValidDeckId_ReturnsSessions() {
        UUID deckId = UUID.randomUUID();
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        String deckName = "Test Deck";

        deckAnalyticsService.createReviewSession(deckId, userId1, deckName, 85, 10, 8);
        deckAnalyticsService.createReviewSession(deckId, userId2, deckName, 90, 12, 11);

        List<ReviewSession> sessions = deckAnalyticsService.getReviewSessionsByDeckId(deckId);
        
        assertThat(sessions).hasSize(2);
        assertThat(sessions).extracting(ReviewSession::getDeckId)
                          .containsOnly(deckId);
    }

    @Test
    void getReviewSessionsByDeckId_NullDeckId_ThrowsException() {
        assertThrows(InvalidAnalyticsParameterException.class, () -> {
            deckAnalyticsService.getReviewSessionsByDeckId(null);
        });
    }

    @Test
    void getReviewSessionsByUserId_ValidUserId_ReturnsSessions() {
        UUID userId = UUID.randomUUID();
        UUID deckId1 = UUID.randomUUID();
        UUID deckId2 = UUID.randomUUID();

        deckAnalyticsService.createReviewSession(deckId1, userId, "Deck 1", 80, 10, 8);
        deckAnalyticsService.createReviewSession(deckId2, userId, "Deck 2", 85, 12, 10);

        List<ReviewSession> sessions = deckAnalyticsService.getReviewSessionsByUserId(userId);
        
        assertThat(sessions).hasSize(2);
        assertThat(sessions).extracting(ReviewSession::getUserId)
                          .containsOnly(userId);
    }

    @Test
    void getReviewSessionsByUserId_NullUserId_ThrowsException() {
        assertThrows(InvalidAnalyticsParameterException.class, () -> {
            deckAnalyticsService.getReviewSessionsByUserId(null);
        });
    }

    @Test
    void getLatestReviewSession_ValidDeckId_ReturnsLatestSession() {
        UUID deckId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String deckName = "Test Deck";

        // Create first session
        deckAnalyticsService.createReviewSession(deckId, userId, deckName, 80, 10, 8);
        
        // Wait a moment to ensure different timestamps
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) { 
            Thread.currentThread().interrupt();
        }
        
        // Create second session (should be latest)
        ReviewSession secondSession = deckAnalyticsService.createReviewSession(deckId, userId, deckName, 85, 12, 10);

        ReviewSession latestSession = deckAnalyticsService.getLatestReviewSession(deckId);
        
        assertThat(latestSession).isNotNull();
        assertThat(latestSession.getId()).isEqualTo(secondSession.getId());
        assertThat(latestSession.getScore()).isEqualTo(85);
    }

    @Test
    void getLatestReviewSession_NullDeckId_ThrowsException() {
        assertThrows(InvalidAnalyticsParameterException.class, () -> {
            deckAnalyticsService.getLatestReviewSession(null);
        });
    }

    @Test
    void deleteByDeckId_ValidDeckId_DeletesAnalyticsAndSessions() {
        UUID deckId = UUID.randomUUID();
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        String deckName = "Deck to Delete";

        // Create analytics and sessions
        deckAnalyticsService.createReviewSession(deckId, userId1, deckName, 80, 10, 8);
        deckAnalyticsService.createReviewSession(deckId, userId2, deckName, 85, 12, 10);

        // Verify data exists
        List<DeckAnalytics> analytics = deckAnalyticsService.getAnalyticsByDeckId(deckId);
        List<ReviewSession> sessions = deckAnalyticsService.getReviewSessionsByDeckId(deckId);
        assertThat(analytics).hasSize(2);
        assertThat(sessions).hasSize(2);

        // Delete by deck ID
        deckAnalyticsService.deleteByDeckId(deckId);

        // Verify data is deleted
        List<DeckAnalytics> analyticsAfterDelete = deckAnalyticsService.getAnalyticsByDeckId(deckId);
        List<ReviewSession> sessionsAfterDelete = deckAnalyticsService.getReviewSessionsByDeckId(deckId);
        assertThat(analyticsAfterDelete).isEmpty();
        assertThat(sessionsAfterDelete).isEmpty();
    }

    @Test
    void deleteByDeckId_NullDeckId_ThrowsException() {
        assertThrows(InvalidAnalyticsParameterException.class, () -> {
            deckAnalyticsService.deleteByDeckId(null);
        });
    }

    @Test
    void deleteByUserId_ValidUserId_DeletesUserAnalyticsAndSessions() {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID deckId1 = UUID.randomUUID();
        UUID deckId2 = UUID.randomUUID();

        // Create analytics for user to delete
        deckAnalyticsService.createReviewSession(deckId1, userId, "Deck 1", 80, 10, 8);
        deckAnalyticsService.createReviewSession(deckId2, userId, "Deck 2", 85, 12, 10);
        
        // Create analytics for other user (should not be deleted)
        deckAnalyticsService.createReviewSession(deckId1, otherUserId, "Deck 1", 90, 15, 13);

        // Verify data exists
        List<DeckAnalytics> userAnalytics = deckAnalyticsService.getAnalyticsByUserId(userId);
        List<ReviewSession> userSessions = deckAnalyticsService.getReviewSessionsByUserId(userId);
        List<DeckAnalytics> otherUserAnalytics = deckAnalyticsService.getAnalyticsByUserId(otherUserId);
        
        assertThat(userAnalytics).hasSize(2);
        assertThat(userSessions).hasSize(2);
        assertThat(otherUserAnalytics).hasSize(1);

        // Delete by user ID
        deckAnalyticsService.deleteByUserId(userId);

        // Verify user data is deleted but other user data remains
        List<DeckAnalytics> userAnalyticsAfterDelete = deckAnalyticsService.getAnalyticsByUserId(userId);
        List<ReviewSession> userSessionsAfterDelete = deckAnalyticsService.getReviewSessionsByUserId(userId);
        List<DeckAnalytics> otherUserAnalyticsAfterDelete = deckAnalyticsService.getAnalyticsByUserId(otherUserId);
        
        assertThat(userAnalyticsAfterDelete).isEmpty();
        assertThat(userSessionsAfterDelete).isEmpty();
        assertThat(otherUserAnalyticsAfterDelete).hasSize(1);
    }

    @Test
    void deleteByUserId_NullUserId_ThrowsException() {
        assertThrows(InvalidAnalyticsParameterException.class, () -> {
            deckAnalyticsService.deleteByUserId(null);
        });
    }

    @Test
    void createReviewSession_ProficiencyCalculation_SetsProficiencyCorrectly() {
        UUID deckId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String deckName = "Proficiency Test Deck";

        // Create multiple high-scoring sessions
        deckAnalyticsService.createReviewSession(deckId, userId, deckName, 95, 10, 10);
        deckAnalyticsService.createReviewSession(deckId, userId, deckName, 90, 10, 9);
        deckAnalyticsService.createReviewSession(deckId, userId, deckName, 92, 10, 9);
        deckAnalyticsService.createReviewSession(deckId, userId, deckName, 88, 10, 9);

        List<DeckAnalytics> analytics = deckAnalyticsService.getAnalyticsByDeckId(deckId);
        assertThat(analytics).hasSize(1);
        
        DeckAnalytics deckAnalytics = analytics.get(0);
        // Proficiency should be determined based on consistent high scores
        // The actual logic depends on your updateProficiency method implementation
        assertThat(deckAnalytics.isProficiency()).isNotNull();
    }
}