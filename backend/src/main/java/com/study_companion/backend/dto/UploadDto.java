package com.study_companion.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import com.study_companion.backend.model.FileType;
import com.study_companion.backend.model.ParsingStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UploadDto {
        @Schema(description = "Response returned of upload object", name = "UploadGetResponse")
        public record GetResponse(
                        @NotNull @Schema(description = "The upload's id", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") UUID id,
                        @NotNull @Schema(description = "The user's id that owns the upload", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") UUID userId,
                        @NotNull @Schema(description = "The deck's id that owns the upload", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") UUID deckId,
                        @NotBlank(message = "File name is required") @Schema(description = "The upload's file name", example = "") String fileName,
                        @NotBlank(message = "File url is required") @Schema(description = "The upload's file url", example = "") String fileUrl,
                        @Schema(description = "The upload's file type", example = "PNG") FileType fileType,
                        @Schema(description = "Whether the upload has been parsed", example = "true") boolean isParsed,
                        @Schema(description = "The upload's parsing status", example = "PENDING") ParsingStatus parsingStatus,
                        @Schema(description = "Possible error message", example = "Failed to parse upload") String errorMessage,
                        @Schema(description = "Timestamp when the parsing was started", example = "2026-02-23T15:32:22.542Z") LocalDateTime parsingStartedAt,
                        @Schema(description = "Timestamp when the parsing was finished", example = "2026-02-23T15:32:22.542Z") LocalDateTime parsingCompletedAt,
                        @Schema(description = "Timestamp when the user was created", example = "2026-02-23T15:32:22.542Z") LocalDateTime createdAt,
                        @Schema(description = "Timestamp when the user was last updated", example = "2026-02-23T15:32:22.542Z") LocalDateTime updatedAt) {
        }

        @Schema(description = "Request body to create new upload", name = "UploadCreateResponse")
        public record CreateRequest(
                        @NotNull @Schema(description = "The user's id that owns the upload", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") UUID userId,
                        @NotNull @Schema(description = "The deck's id that owns the upload", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") UUID deckId,
                        @NotBlank(message = "File name is required") @Schema(description = "The upload's file name", example = "") String fileName,
                        @Schema(description = "The upload's file type", example = "PNG") FileType fileType,
                        @NotBlank(message = "File url is required") @Schema(description = "The upload's file url", example = "") String fileUrl) {
        }
}
