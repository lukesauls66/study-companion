package com.lukesauls.studycompanion.studycompanion_backend.model.postgres;

import jakarta.validation.constraints.*;
import jakarta.persistence.*;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "decks")
public class Deck {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String title;

    private String description;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "deck", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Card> cards = new ArrayList<>();

    @OneToMany(mappedBy = "deck", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Upload> uploads = new ArrayList<>();

    // Constructors
    public Deck() {
    }

    public Deck(User user, String title, String description) {
        this.user = user;
        this.title = title;
        this.description = description;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<Card> getCards() {
        return cards;
    }

    //Helpers for managing bi-directional relationship
    public void addCard(Card card) {
        cards.add(card);
        card.setDeck(this);
    }

    public void removeCard(Card card) {
        cards.remove(card);
        card.setDeck(null);
    }

    public void addUpload(Upload upload) {
        uploads.add(upload);
        upload.setDeck(this);
    }

    public void removeUpload(Upload upload) {
        uploads.remove(upload);
        upload.setDeck(null);
    }

    //Lifecycle Callbacks
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
