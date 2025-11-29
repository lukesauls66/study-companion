package com.lukesauls.studycompanion.studycompanion_backend.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

public class CardDto{
    public record Create(@NotNull UUID deckId, @NotNull String question, @NotNull String answer, String imageUrl) {}

    public record Update(String question, String answer, String imageUrl) {}
}
