package com.study_companion.backend.model.redis;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class DeckCache implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID deckId;
    private String title;
    private String description;
    private int cardCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public DeckCache() {
        this.cardCount = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public DeckCache(UUID deckId, String title, String description) {
        this();
        this.deckId = deckId;
        this.title = title;
        this.description = description;
    }

    public DeckCache(UUID deckId, String title, String description, int cardCount) {
        this(deckId, title, description);
        this.cardCount = cardCount;
    }

    // Getters and Setters
    public UUID getDeckId() {
        return deckId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getCardCount() {
        return cardCount;
    }

    public void setCardCount(int cardCount) {
        this.cardCount = cardCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "DeckCache{" +
                "deckId='" + deckId + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", cardCount=" + cardCount +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}