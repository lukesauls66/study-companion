package com.lukesauls.studycompanion.studycompanion_backend.model.redis;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

@RedisHash(value = "cached_decks", timeToLive = 86400) // 24 hours TTL - matches session duration 
public class CachedDecks implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    private UUID userId;
    
    @NotNull
    private List<DeckCache> decks;
    
    private LocalDateTime cachedAt;
    
    private LocalDateTime lastModified;

    // Constructors
    public CachedDecks() {
        this.decks = new ArrayList<>();
        this.cachedAt = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
    }

    public CachedDecks(UUID userId) {
        this();
        this.userId = userId;
    }

    public CachedDecks(UUID userId, List<DeckCache> decks) {
        this(userId);
        this.decks = decks != null ? decks : new ArrayList<>();
    }

    // Getters and Setters
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public List<DeckCache> getDecks() {
        return decks;
    }

    public void setDecks(List<DeckCache> decks) {
        this.decks = decks;
        this.lastModified = LocalDateTime.now();
    }

    public LocalDateTime getCachedAt() {
        return cachedAt;
    }

    public void setCachedAt(LocalDateTime cachedAt) {
        this.cachedAt = cachedAt;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    // Helper methods
    public void addDeck(DeckCache deck) {
        if (this.decks == null) {
            this.decks = new ArrayList<>();
        }
        this.decks.add(deck);
        this.lastModified = LocalDateTime.now();
    }

    public void removeDeck(UUID deckId) {
        if (this.decks != null) {
            this.decks.removeIf(deck -> deck.getDeckId().equals(deckId));
            this.lastModified = LocalDateTime.now();
        }
    }

    public DeckCache findDeckById(UUID deckId) {
        if (this.decks == null) {
            return null;
        }
        return this.decks.stream()
                .filter(deck -> deck.getDeckId().equals(deckId))
                .findFirst()
                .orElse(null);
    }

    public void updateDeck(DeckCache updatedDeck) {
        if (this.decks != null) {
            for (int i = 0; i < this.decks.size(); i++) {
                if (this.decks.get(i).getDeckId().equals(updatedDeck.getDeckId())) {
                    this.decks.set(i, updatedDeck);
                    this.lastModified = LocalDateTime.now();
                    break;
                }
            }
        }
    }

    public int getDeckCount() {
        return this.decks != null ? this.decks.size() : 0;
    }

    public boolean isEmpty() {
        return this.decks == null || this.decks.isEmpty();
    }

    @Override
    public String toString() {
        return "CachedDecks{" +
                "userId=" + userId +
                ", deckCount=" + getDeckCount() +
                ", cachedAt=" + cachedAt +
                ", lastModified=" + lastModified +
                '}';
    }


}