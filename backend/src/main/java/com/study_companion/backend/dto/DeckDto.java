package com.study_companion.backend.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DeckDto {
        @Schema(description = "Response returned of deck object", name = "DeckGetResponse")
        public record GetResponse(
                        @NotNull @Schema(description = "The deck's id", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") UUID deckId,
                        @NotNull @Schema(description = "The user id that owns this deck", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") UUID userId,
                        @NotBlank(message = "Title is required") @Schema(description = "The deck's title", example = "Physics 1") String title,
                        @NotBlank(message = "Description is required") @Schema(description = "The deck's description", example = "Velocity vectors") String description,
                        @Schema(description = "Timestamp when the deck was created", example = "2026-02-23T15:32:22.542Z") LocalDateTime createdAt,
                        @Schema(description = "Timestamp when the deck was last updated", example = "2026-02-23T15:32:22.542Z") LocalDateTime updatedAt) {
        }

        @Schema(description = "Response returned of deck object", name = "ExtendedDeckGetResponse")
        public record GetResponseWithCardsAndUploads(
                        @NotNull @Schema(description = "The deck's id", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") UUID deckId,
                        @NotNull @Schema(description = "The user id that owns this deck", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") UUID userId,
                        @NotBlank(message = "Title is required") @Schema(description = "The deck's title", example = "Physics 1") String title,
                        @NotBlank(message = "Description is required") @Schema(description = "The deck's description", example = "Velocity vectors") String description,
                        @Schema(description = "Timestamp when the deck was created", example = "2026-02-23T15:32:22.542Z") LocalDateTime createdAt,
                        @Schema(description = "Timestamp when the deck was last updated", example = "2026-02-23T15:32:22.542Z") LocalDateTime updatedAt,
                        @Schema(description = "All cards belonging to this deck") List<CardDto.GetResponse> cards,
                        @Schema(description = "All uploads belonging to this deck") List<UploadDto.GetResponse> uploads) {
        }

        @Schema(description = "Request body to create new deck", name = "DeckCreateRequest")
        public record CreateRequest(
                        @NotNull @Schema(description = "The user id that owns this deck", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") UUID userId,
                        @NotBlank(message = "Title is required") @Schema(description = "The deck's title", example = "Physics 1") String title,
                        @NotBlank(message = "Description is required") @Schema(description = "The deck's description", example = "Velocity vectors") String description) {
        }

        @Schema(description = "Request body to update deck", name = "DeckUpdateRequest")
        public record UpdateRequest(@Schema(description = "The deck's title", example = "Physics 1") String title,
                        @Schema(description = "The deck's description", example = "Velocity vectors") String description) {
        }
}
