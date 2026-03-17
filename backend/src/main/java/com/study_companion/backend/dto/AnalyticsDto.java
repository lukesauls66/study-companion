package com.study_companion.backend.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AnalyticsDto {
    public record CreateReview(
            @NotNull @Schema(description = "The user's id", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") UUID deckId,
            @NotNull @Schema(description = "The user's id", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") UUID userId,
            @NotBlank(message = "Deck name is required") @Schema(description = "The deck's name", example = "Physics Test 1") String deckName,
            @NotNull @Schema(description = "The score out of 100", example = "92") int score,
            @NotNull @Schema(description = "The number of cards reviewed this session", example = "25") int cardsReviewed,
            @NotNull @Schema(description = "The number of correct answers this session", example = "22") int correctAnswers) {
    }
}
