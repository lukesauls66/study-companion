package com.lukesauls.studycompanion.studycompanion_backend.dto;

import java.util.UUID;

public record DeckDto(UUID userId, String title, String description) {}
