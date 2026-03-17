package com.study_companion.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.study_companion.backend.model.CardCreationType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CardDto {
        @Schema(description = "Response returned of card object", name = "CardGetResponse")
        public record GetResponse(
                        @NotNull @Schema(description = "The card's id", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") UUID id,
                        @NotNull @Schema(description = "The deck's id that owns the card", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") UUID deckId,
                        @NotBlank(message = "Question is required") @Schema(description = "The card's question", example = "2 + 2") String question,
                        @NotBlank(message = "Answer is required") @Schema(description = "The card's answer", example = "4") String answer,
                        @Schema(description = "The card's image", example = "String") String imageUrl,
                        @Schema(description = "The card's creation type", example = "MANUAL_UPLOAD") CardCreationType creationType,
                        @Schema(description = "Timestamp when the user was created", example = "2026-02-23T15:32:22.542Z") LocalDateTime createdAt,
                        @Schema(description = "Timestamp when the user was last updated", example = "2026-02-23T15:32:22.542Z") LocalDateTime updatedAt) {
        }

        @Schema(description = "Request body to create new card", name = "CardCreateRequest")
        public record CreateRequest(
                        @NotNull @Schema(description = "The deck id of the new card", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") UUID deckId,
                        @NotBlank(message = "Question is required") @Schema(description = "The new card's question", example = "2 + 2") String question,
                        @NotBlank(message = "Answer is required") @Schema(description = "The new card's answer", example = "4") String answer,
                        @Schema(description = "The new card's image", example = "String") String imageUrl) {
        }

        @Schema(description = "Request body to update card", name = "CardUpdateRequest")
        public record UpdateRequest(
                        @Schema(description = "The card's updated question", example = "2 + 2") String question,
                        @Schema(description = "The card's updated answer", example = "4") String answer,
                        @Schema(description = "The card's updated image", example = "String") String imageUrl) {
        }
}
