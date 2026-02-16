package com.study_companion.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.study_companion.backend.model.Role;

import jakarta.validation.constraints.NotNull;
 
public class UserDto{
    public record Get(@NotNull UUID id, @NotNull String email, @NotNull String name, @NotNull String username, Role role, boolean isVerified, LocalDateTime createdAt, LocalDateTime updatedAt) {}

    public record Create(@NotNull String email, @NotNull String name, @NotNull String username, @NotNull String password) {}

    public record Update(String email, String name, String username) {}

    public record ChangePassword(@NotNull String currPassword, @NotNull String newPassword, @NotNull String confirmNewPassword) {}
}
