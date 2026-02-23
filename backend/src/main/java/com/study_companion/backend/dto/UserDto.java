package com.study_companion.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.study_companion.backend.model.Role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UserDto {
    @Schema(description = "Request body to create new user")
    public record CreateRequest(
            @NotBlank(message = "Email is required") @Schema(description = "The email of the new user", example = "johnny123@example.com") String email,
            @NotBlank(message = "Name is required") @Schema(description = "The name of the new user", example = "johnny") String name,
            @NotBlank(message = "Username is required") @Schema(description = "The username of the user", example = "johnny123") String username,
            @NotBlank(message = "Password is required") @Schema(description = "The password of the new user", example = "password12!") String rawPassword) {
    }

    public record Get(@NotNull UUID id, @NotNull String email, @NotNull String name, @NotNull String username,
            Role role, boolean isVerified, LocalDateTime createdAt, LocalDateTime updatedAt) {
    }

    public record Update(String email, String name, String username) {
    }

    public record ChangePassword(@NotNull String currPassword, @NotNull String newRawPassword,
            @NotNull String confirmNewRawPassword) {
    }
}
