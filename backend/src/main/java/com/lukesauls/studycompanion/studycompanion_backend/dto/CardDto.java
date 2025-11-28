package com.lukesauls.studycompanion.studycompanion_backend.dto;

import java.util.UUID;

public record CardDto(UUID deckId, String question, String answer, String imageUrl) {}
