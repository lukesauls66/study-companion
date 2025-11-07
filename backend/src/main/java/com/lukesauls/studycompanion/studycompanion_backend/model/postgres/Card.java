package com.lukesauls.studycompanion.studycompanion_backend.model.postgres;

import com.lukesauls.studycompanion.studycompanion_backend.model.CardCreationType;
import jakarta.persistence.*;
import java.util.UUID;
import java.time.LocalDateTime;

@Entity
@Table(name = "cards")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deckId", nullable = false)
    private Deck deck;

    @Column(nullable = false)
    private String question;

    @Column(nullable = false)
    private String answer;

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardCreationType creationType;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Constructors
    public Card() {
    }

    public Card(Deck deck, String question, String answer, CardCreationType creationType) {
        this.deck = deck;
        this.question = question;
        this.answer = answer;
        this.creationType = creationType;
    }

    public Card(Deck deck, String question, String answer, CardCreationType creationType, String imageUrl) {
        this(deck, question, answer, creationType);
        this.imageUrl = imageUrl;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public Deck getDeck() {
        return deck;
    }

    public void setDeck(Deck deck) {
        this.deck = deck;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public CardCreationType getCreationType() {
        return creationType;
    }

    public void setCreationType(CardCreationType creationType) {
        this.creationType = creationType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
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