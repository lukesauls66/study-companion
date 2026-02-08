package com.study_companion.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.study_companion.backend.exception.analytics.AnalyticsOperationException;
import com.study_companion.backend.exception.analytics.InvalidAnalyticsParameterException;
import com.study_companion.backend.model.mongo.DeckAnalytics;
import com.study_companion.backend.model.mongo.ReviewSession;
import com.study_companion.backend.repository.mongo.DeckAnalyticsRepository;
import com.study_companion.backend.repository.mongo.ReviewSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DeckAnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(DeckAnalyticsService.class);

    private final DeckAnalyticsRepository deckAnalyticsRepository;

    private final ReviewSessionRepository reviewSessionRepository;

    DeckAnalyticsService(DeckAnalyticsRepository deckAnalyticsRepository, ReviewSessionRepository reviewSessionRepository) {
        this.deckAnalyticsRepository = deckAnalyticsRepository;
        this.reviewSessionRepository = reviewSessionRepository;
    }

    /**
     * Creates a new review session and automatically updates the corresponding deck
     * analytics
     * 
     * @param deckId         The ID of the deck being reviewed
     * @param userId         The ID of the user performing the review
     * @param deckName       The name of the deck
     * @param score          The score achieved in this session
     * @param cardsReviewed  Total number of cards reviewed
     * @param correctAnswers Number of correct answers
     * @return The created ReviewSession
     * @throws InvalidAnalyticsParameterException if and nonnull args are null, or
     *                                            if integer args are not greater
     *                                            than 0
     * @throws AnalyticsOperationException        if server error occurs
     */
    @Transactional
    public ReviewSession createReviewSession(UUID deckId, UUID userId,
            String deckName, int score,
            int cardsReviewed, int correctAnswers) {
        if (deckId == null) {
            throw new InvalidAnalyticsParameterException("deckId cannot be null");
        }

        if (userId == null) {
            throw new InvalidAnalyticsParameterException("userId cannot be null");
        }

        if (deckName == null) {
            throw new InvalidAnalyticsParameterException("Deck name cannot be null");
        }

        try {
            logger.debug("Creating review session for deck {} by user {}", deckId, userId);

            ReviewSession reviewSession = new ReviewSession(deckId, userId, deckName, score, cardsReviewed,
                    correctAnswers);
            ReviewSession savedSession = reviewSessionRepository.save(reviewSession);
            logger.debug("Saved review session with ID: {}", savedSession.getId());

            logger.debug("Fetching deck analytics for deck {}", deckId);
            DeckAnalytics analytics = findOrCreateDeckAnalytics(deckId, userId);

            analytics.addReviewSession(savedSession);

            updateProficiency(analytics);

            deckAnalyticsRepository.save(analytics);
            logger.info("Updated deck analytics for deck {} user {}", deckId, userId);

            return savedSession;
        } catch (Exception e) {
            logger.error("Failed to create the review session: {}", e.getMessage());
            throw new AnalyticsOperationException("Failed to create the review session", e);
        }
    }

    /**
     * Finds existing deck analytics or creates new ones
     * 
     * @param deckId The ID of the deck being reviewed
     * @param userId The ID of the user performing the review
     * @return the user's deck analytics
     * @throws InvalidAnalyticsParameterException if and nonnull args are null
     * @throws AnalyticsOperationException        if server error occurs
     */
    private DeckAnalytics findOrCreateDeckAnalytics(UUID deckId, UUID userId) {
        if (deckId == null) {
            throw new InvalidAnalyticsParameterException("deckId cannot be null");
        }

        if (userId == null) {
            throw new InvalidAnalyticsParameterException("userId cannot be null");
        }

        try {
            Optional<DeckAnalytics> existingAnalytic = deckAnalyticsRepository.findByDeckIdAndUserId(deckId, userId);

            if (existingAnalytic.isPresent()) {
                logger.debug("Found existing analytics for deck {} user {}", deckId, userId);
                return existingAnalytic.get();
            } else {
                logger.debug("Creating new analytics for deck {} user {}", deckId, userId);
                return new DeckAnalytics(deckId, userId);
            }
        } catch (Exception e) {
            logger.error("Failed to fetch or create analytics: {}", e.getMessage());
            throw new AnalyticsOperationException("Failed to fetch or create analytics", e);
        }
    }

    /**
     * Updates proficiency based on recent performance
     * A deck is considered proficient if the average score of the last 3 sessions
     * is >= 80
     * 
     * @param analytics the deck analytics to be updated
     * @return the user's deck analytics
     * @throws InvalidAnalyticsParameterException if and nonnull args are null
     * @throws AnalyticsOperationException        if server error occurs
     */
    private void updateProficiency(DeckAnalytics analytics) {
        if (analytics == null) {
            throw new InvalidAnalyticsParameterException("Analytics cannot be null");
        }

        try {
            List<ReviewSession> sessions = analytics.getReviewSessions();
            if (sessions.size() >= 3) {
                List<ReviewSession> recentSessions = sessions.subList(sessions.size() - 3, sessions.size());
                double avgScore = recentSessions.stream()
                        .mapToInt(ReviewSession::getScore)
                        .average()
                        .orElse(0.0);

                analytics.setProficiency(avgScore >= 80.0);
                logger.debug("Updated proficiency to {} based on avg score: {}", analytics.isProficiency(), avgScore);
            }
        } catch (Exception e) {
            logger.error("Failed to update the decks proficiency: {}", e.getMessage());
            throw new AnalyticsOperationException("Failed to update the decks proficiency", e);
        }
    }

    /**
     * Get deck analytics by deck ID
     * 
     * @param deckId the ID of the deck who's analytics are being fetched
     * @return A list of all analytics associated with said deck
     * @throws InvalidAnalyticsParameterException if any nonnull args are null
     * @throws AnalyticsOperationException        if server error occurs
     */
    public List<DeckAnalytics> getAnalyticsByDeckId(UUID deckId) {
        if (deckId == null) {
            throw new InvalidAnalyticsParameterException("deckId cannot be null");
        }

        try {
            return deckAnalyticsRepository.findByDeckId(deckId);
        } catch (Exception e) {
            logger.error("Failed to fetch deck analytics by provided deckId: {}", e.getMessage());
            throw new AnalyticsOperationException("Failed to fetch deck analytics by provided deckId", e);
        }
    }

    /**
     * Get deck analytics by user ID
     * 
     * @param userId the ID of whose analytics to fetch
     * @return all analytics belonging to the provided userId
     * @throws InvalidAnalyticsParameterException if any nonnull args are null
     * @throws AnalyticsOperationException        if server error occurs
     */
    public List<DeckAnalytics> getAnalyticsByUserId(UUID userId) {
        if (userId == null) {
            throw new InvalidAnalyticsParameterException("userId cannot be null");
        }

        try {
            return deckAnalyticsRepository.findByUserId(userId);
        } catch (Exception e) {
            logger.error("Failed to fetch all analytics belonging to the provided user: {}", e.getMessage());
            throw new AnalyticsOperationException("Failed to fetch all analytics belonging to the provided user", e);
        }
    }

    /**
     * Get deck analytics by user ID ordered by highest score
     * 
     * @param userId the ID of whose analytics to fetch
     * @return all analytics belonging to the provided userId in order of highest
     *         score
     * @throws InvalidAnalyticsParameterException if any nonnull args are null
     * @throws AnalyticsOperationException        if server error occurs
     */
    public List<DeckAnalytics> getAnalyticsByUserIdOrderByScore(UUID userId) {
        if (userId == null) {
            throw new InvalidAnalyticsParameterException("userId cannot be null");
        }

        try {
            return deckAnalyticsRepository.findByUserIdOrderByHighestScoreDesc(userId);
        } catch (Exception e) {
            logger.error("Failed to fetch analytics by high score for the provided user: {}", e.getMessage());
            throw new AnalyticsOperationException("Failed to fetch analytics by high score for the provided user", e);
        }
    }

    /**
     * Get proficient decks for a user
     * 
     * @param userId the ID of whose analytics to fetch
     * @return all proficient analytics belonging to the provided userId
     * @throws InvalidAnalyticsParameterException if any nonnull args are null
     * @throws AnalyticsOperationException        if server error occurs
     */
    public List<DeckAnalytics> getProficientDecks(UUID userId) {
        if (userId == null) {
            throw new InvalidAnalyticsParameterException("userId cannot be null");
        }

        try {
            return deckAnalyticsRepository.findByUserIdAndProficiency(userId, true);
        } catch (Exception e) {
            logger.error("Failed to fetch all proficient decks for the provided user: {}", e.getMessage());
            throw new AnalyticsOperationException("Failed to fetch all proficient decks for the provided user", e);
        }
    }

    /**
     * Get all review sessions for a deck
     * 
     * @param deckId the ID of the deck whose review sessions are being fetched
     * @return all review sessions belonging to the provided deckId
     * @throws InvalidAnalyticsParameterException if any nonnull args are null
     * @throws AnalyticsOperationException        if server error occurs
     */
    public List<ReviewSession> getReviewSessionsByDeckId(UUID deckId) {
        if (deckId == null) {
            throw new InvalidAnalyticsParameterException("deckId cannot be null");
        }

        try {
            return reviewSessionRepository.findByDeckId(deckId);
        } catch (Exception e) {
            logger.error("Failed to fetch all review sessions belonging to the provided deckId: {}", e.getMessage());
            throw new AnalyticsOperationException(
                    "Failed to fetch all review sessions belonging to the provided deckId", e);
        }
    }

    /**
     * Get all review sessions for a user
     * 
     * @param userId the ID of the user whose review sessions are being fetched
     * @return all review sessions belonging to the provided userId
     * @throws InvalidAnalyticsParameterException if any nonnull args are null
     * @throws AnalyticsOperationException        if server error occurs
     */
    public List<ReviewSession> getReviewSessionsByUserId(UUID userId) {
        if (userId == null) {
            throw new InvalidAnalyticsParameterException("userId cannot be null");
        }

        try {
            return reviewSessionRepository.findByUserId(userId);
        } catch (Exception e) {
            logger.error("Failed to fetch all of the review sessions belonging to the provided user: {}",
                    e.getMessage());
            throw new AnalyticsOperationException(
                    "Failed to fetch all of the review sessions belonging to the provided user", e);
        }
    }

    /**
     * Get the latest review session for a deck
     * 
     * @param deckId the ID of the deck whose review session is being fetched
     * @return the latest review session belonging to the provided deckId
     * @throws InvalidAnalyticsParameterException if any nonnull args are null
     * @throws AnalyticsOperationException        if server error occurs
     */
    public ReviewSession getLatestReviewSession(UUID deckId) {
        if (deckId == null) {
            throw new InvalidAnalyticsParameterException("deckId cannot be null");
        }

        try {
            return reviewSessionRepository.findTopByDeckIdOrderByDateDesc(deckId);
        } catch (Exception e) {
            logger.error("Failed to fetch the latest review session belonging to the provided deckId: {}",
                    e.getMessage());
            throw new AnalyticsOperationException(
                    "Failed to fetch the latest review session belonging to the provided deckId", e);
        }
    }

    /**
     * Delete all analytics and sessions for a deck
     * 
     * @param deckId the ID of the deck whose review sessions and analytics are to
     *               be deleted
     * @throws InvalidAnalyticsParameterException if any nonnull args are null
     * @throws AnalyticsOperationException        if server error occurs
     */
    @Transactional
    public void deleteByDeckId(UUID deckId) {
        if (deckId == null) {
            throw new InvalidAnalyticsParameterException("deckId cannot be null");
        }

        try {
            logger.info("Deleting all analytics and sessions for deck: {}", deckId);
            reviewSessionRepository.deleteByDeckId(deckId);
            deckAnalyticsRepository.deleteByDeckId(deckId);
            logger.info("Successfully deleted all analytics and sessions for this deck");
        } catch (Exception e) {
            logger.error(
                    "Failed to delete all review sessions and analytics belonging to the provided deckId. Rolling back content: {}",
                    e.getMessage());
            throw new AnalyticsOperationException(
                    "Failed to delete all review sessions and analytics belonging to the provided deckId. Rolling back content",
                    e);
        }
    }

    /**
     * Delete all analytics and sessions for a user
     * 
     * @param userId the ID of the user whose review sessions and analytics are
     *               being deleted
     * @throws InvalidAnalyticsParameterException if any nonnull args are null
     * @throws AnalyticsOperationException        if server error occurs
     */
    @Transactional
    public void deleteByUserId(UUID userId) {
        if (userId == null) {
            throw new InvalidAnalyticsParameterException("userId cannot be null");
        }

        try {
            logger.info("Deleting all analytics and sessions for user: {}", userId);
            reviewSessionRepository.deleteByUserId(userId);
            deckAnalyticsRepository.deleteByUserId(userId);
            logger.info("Successfully deleted all analytics and sessions for this user");
        } catch (Exception e) {
            logger.error(
                    "Failed to delete all review sessions and analytics belonging to the provided userId. Rolling back content: {}",
                    e.getMessage());
            throw new AnalyticsOperationException(
                    "Failed to delete all review sessions and analytics belonging to the provided userId. Rolling back content",
                    e);
        }
    }
}
