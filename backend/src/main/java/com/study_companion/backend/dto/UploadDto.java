package com.study_companion.backend.dto;

import java.util.UUID;
import com.study_companion.backend.model.FileType;

import jakarta.validation.constraints.NotNull;

public class UploadDto{
    public record Create(@NotNull UUID userId, @NotNull UUID deckId, @NotNull String fileName, @NotNull FileType fileType) {}
}
