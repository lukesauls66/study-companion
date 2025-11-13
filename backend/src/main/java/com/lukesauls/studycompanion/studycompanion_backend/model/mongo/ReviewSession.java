package com.lukesauls.studycompanion.studycompanion_backend.model.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "review_sessions")
public class ReviewSession {
    @Id
    private UUID id;

    @Field("date")
    @NotNull
    private LocalDateTime date;

    @Field("deck_name")
    @NotBlank
    private String deckName;

    @Field("score")
    @Min(0)
    private int score;

    @Field("cards_reviewed")
    @Min(0)
    private int cardsReviewed;

    @Field("correct_answers")
    @Min(0)
    private int correctAnswers;

    //Constructors
    public ReviewSession() {
        this.id = UUID.randomUUID();
        this.date = LocalDateTime.now();
    }

    public ReviewSession(String deckName, int score, int cardsReviewed, int correctAnswers) {
        this();
        this.deckName = deckName;
        this.score = score;
        this.cardsReviewed = cardsReviewed;
        this.correctAnswers = correctAnswers;
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getDeckName() {
        return deckName;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getCardsReviewed() {
        return cardsReviewed;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    @Override
    public String toString() {
        return "ReviewSession{" +
                "id='" + id + '\'' +
                ", date=" + date +
                ", deckName='" + deckName + '\'' +
                ", score=" + score +
                ", cardsReviewed=" + cardsReviewed +
                ", correctAnswers=" + correctAnswers +
                '}';
    }
}
