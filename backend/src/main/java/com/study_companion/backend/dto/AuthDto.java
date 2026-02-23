package com.study_companion.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class AuthDto {
        @Schema(description = "Request body to login")
        public record LoginRequest(
                        @NotBlank(message = "Username is required") @Schema(description = "The username of the user", example = "johnny123") String username,
                        @NotBlank(message = "Password is required") @Schema(description = "The user's password", example = "Password123!") String password) {
        }

        @Schema(description = "Response returned after a login attempt")
        public record LoginResponse(
                        @Schema(description = "Result message", example = "Login successful") String message) {
        }

        @Schema(description = "Response returned after a registration attempt")
        public record RegisterResponse(
                        @Schema(description = "Result message", example = "User registered successfully") String message) {
        }

        @Schema(description = "Response returned after a successful logout")
        public record LogoutResponse(
                        @Schema(description = "Result message", example = "Logged out successfully") String message) {
        }
}
