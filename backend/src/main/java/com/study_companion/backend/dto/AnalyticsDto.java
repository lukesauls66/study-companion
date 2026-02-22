package com.study_companion.backend.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

public class AnalyticsDto {
    public record GetAnalytics(@NotNull UUID deckId, @NotNull UUID userId) {}
    
    public record CreateReview(@NotNull UUID deckId, @NotNull UUID userId, @NotNull String deckName, @NotNull int score, @NotNull int cardsReviewed,
            @NotNull int correctAnswers) {
    }
}
