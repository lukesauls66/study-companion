package com.lukesauls.studycompanion.studycompanion_backend.model.redis;

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
    public DeckCache() {}

    public DeckCache(UUID deckId, String title, String description, int cardCount) {
        this.deckId = deckId;
        this.title = title;
        this.description = description;
        this.cardCount = cardCount;
    }

    // Getters and Setters
    public UUID getDeckId() {
        return deckId;
    }

    public void setDeckId(UUID deckId) {
        this.deckId = deckId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "DeckCache{" +
                "deckId=" + deckId +
                ", title='" + title + '\'' +
                ", cardCount=" + cardCount +
                ", lastStudied=" + lastStudied +
                '}';
    }
}