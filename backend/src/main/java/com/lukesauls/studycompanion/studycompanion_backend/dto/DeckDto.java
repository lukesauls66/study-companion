package com.lukesauls.studycompanion.studycompanion_backend.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

public class DeckDto{
    public record Create(@NotNull UUID userId, @NotNull String title, @NotNull String description) {}

    public record Update(String title, String description) {}
}
