package com.lukesauls.studycompanion.studycompanion_backend.dto;

import java.util.UUID;
import com.lukesauls.studycompanion.studycompanion_backend.model.FileType;

public record UploadDto(UUID deckId, String fileName, FileType fileType) {}
