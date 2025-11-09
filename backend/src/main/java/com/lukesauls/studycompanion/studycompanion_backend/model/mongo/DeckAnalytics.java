package com.lukesauls.studycompanion.studycompanion_backend.model.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

@Document(collection = "deck_analytics")
public class DeckAnalytics {
    @Id
    private UUID id;

    @Field("deck_id")
    @NotNull
    private UUID deckId;

    @Field("user_id")
    @NotNull
    private UUID userId;

    @Field("highest_score")
    @Min(0)
    private int highestScore;

    @Field("highest_score_date")
    private LocalDateTime highestScoreDate;

    @Field("previous_score")
    @Min(0)
    private int previousScore;

    @Field("previous_score_date")
    private LocalDateTime previousScoreDate;

    @Field("review_sessions")
    private List<ReviewSession> reviewSessions;

    @Field("proficiency")
    private boolean proficiency;

    // Constructors
    public DeckAnalytics() {
        this.id = UUID.randomUUID();
        this.reviewSessions = new ArrayList<>();
    }

    public DeckAnalytics(UUID deckId, UUID userId) {
        this.id = UUID.randomUUID();
        this.deckId = deckId;
        this.userId = userId;
        this.highestScore = 0;
        this.previousScore = 0;
        this.reviewSessions = new ArrayList<>();
        this.proficiency = false;
    }

    public DeckAnalytics(UUID deckId, UUID userId, int highestScore, LocalDateTime highestScoreDate, int previousScore,
            LocalDateTime previousScoreDate, List<ReviewSession> reviewSessions, boolean proficiency) {
        this.id = UUID.randomUUID();
        this.deckId = deckId;
        this.userId = userId;
        this.highestScore = highestScore;
        this.highestScoreDate = highestScoreDate;
        this.previousScore = previousScore;
        this.previousScoreDate = previousScoreDate;
        this.reviewSessions = reviewSessions != null ? reviewSessions : new ArrayList<>();
        this.proficiency = proficiency;
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getDeckId() {
        return deckId;
    }

    public void setDeckId(UUID deckId) {
        this.deckId = deckId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public int getHighestScore() {
        return highestScore;
    }

    public void setHighestScore(int highestScore) {
        this.highestScore = highestScore;
    }

    public LocalDateTime getHighestScoreDate() {
        return highestScoreDate;
    }

    public void setHighestScoreDate(LocalDateTime highestScoreDate) {
        this.highestScoreDate = highestScoreDate;
    }

    public int getPreviousScore() {
        return previousScore;
    }

    public void setPreviousScore(int previousScore) {
        this.previousScore = previousScore;
    }

    public LocalDateTime getPreviousScoreDate() {
        return previousScoreDate;
    }

    public void setPreviousScoreDate(LocalDateTime previousScoreDate) {
        this.previousScoreDate = previousScoreDate;
    }

    public List<ReviewSession> getReviewSessions() {
        return reviewSessions;
    }

    public void setReviewSessions(List<ReviewSession> reviewSessions) {
        this.reviewSessions = reviewSessions;
    }

    public boolean isProficiency() {
        return proficiency;
    }

    public void setProficiency(boolean proficiency) {
        this.proficiency = proficiency;
    }

    // Helper methods
    public void addReviewSession(ReviewSession session) {
        if (this.reviewSessions == null) {
            this.reviewSessions = new ArrayList<>();
        }
        this.reviewSessions.add(session);

        if (session.getScore() > this.highestScore) {
            this.highestScore = session.getScore();
            this.highestScoreDate = session.getDate();
        }

        this.previousScore = session.getScore();
        this.previousScoreDate = session.getDate();
    }

    public double getAverageScore() {
        if (reviewSessions == null || reviewSessions.isEmpty()) {
            return 0.0;
        }
        return reviewSessions.stream()
                .mapToInt(ReviewSession::getScore)
                .average()
                .orElse(0.0);
    }

    @Override
    public String toString() {
        return "DeckAnalytics{" +
                "id='" + id + '\'' +
                ", deckId=" + deckId +
                ", userId=" + userId +
                ", highestScore=" + highestScore +
                ", highestScoreDate=" + highestScoreDate +
                ", previousScore=" + previousScore +
                ", previousScoreDate=" + previousScoreDate +
                ", reviewSessionsCount=" + (reviewSessions != null ? reviewSessions.size() : 0) +
                ", proficiency=" + proficiency +
                '}';
    }
}