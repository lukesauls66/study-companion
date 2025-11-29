package com.lukesauls.studycompanion.studycompanion_backend.dto;

import jakarta.validation.constraints.NotNull;

public class UserDto{
    public record Create(@NotNull String email, @NotNull String name, @NotNull String username, @NotNull String password) {}

    public record Update(String email, String name, String username) {}

    public record ChangePassword(@NotNull String currPassword, @NotNull String newPassword) {}
}
