package com.study_companion.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.study_companion.backend.model.Role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UserDto {
        @Schema(description = "Response returned of user object excluding password")
        public record GetResponse(
                        @NotNull @Schema(description = "The user's id", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") UUID id,
                        @NotBlank(message = "Email is required") @Schema(description = "The user's email", example = "johnny123@example.com") String email,
                        @NotBlank(message = "Name is required") @Schema(description = "The user's name", example = "johnny") String name,
                        @NotBlank(message = "Username is required") @Schema(description = "The user's username", example = "johnny123") String username,
                        @Schema(description = "The user's role", example = "ADMIN", allowableValues = {
                                        "USER", "ADMIN" }) Role role,
                        @Schema(description = "The user's verification status", example = "false") boolean isVerified,
                        @Schema(description = "Timestamp when the user was created", example = "2026-02-23T15:32:22.542Z") LocalDateTime createdAt,
                        @Schema(description = "Timestamp when the user was last updated", example = "2026-02-23T15:32:22.542Z") LocalDateTime updatedAt) {
        }

        @Schema(description = "Request body to create new user")
        public record CreateRequest(
                        @NotBlank(message = "Email is required") @Schema(description = "The email of the new user", example = "johnny123@example.com") String email,
                        @NotBlank(message = "Name is required") @Schema(description = "The name of the new user", example = "johnny") String name,
                        @NotBlank(message = "Username is required") @Schema(description = "The username of the user", example = "johnny123") String username,
                        @NotBlank(message = "Password is required") @Schema(description = "The password of the new user", example = "password12!") String rawPassword) {
        }

        public record UpdateRequest(
                        @Schema(description = "The email of the new user", example = "johnny123@example.com") String email,
                        @Schema(description = "The name of the new user", example = "johnny") String name,
                        @Schema(description = "The username of the user", example = "johnny123") String username) {
        }

        public record ChangePasswordRequest(
                        @NotNull(message = "Current password is required") @Schema(description = "The current password of the user", example = "password123!") String currPassword,
                        @NotNull(message = "New password is required") @Schema(description = "The new password of the user", example = "Password456?") String newRawPassword,
                        @NotNull(message = "New password confirmation is required") @Schema(description = "The new password confirmation of the user", example = "Password456?") String confirmNewRawPassword) {
        }
}
