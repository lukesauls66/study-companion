package com.lukesauls.studycompanion.studycompanion_backend.exception;

public class DeckNotFoundException extends RuntimeException {
    public DeckNotFoundException(String message) {
        super(message);
    }
}
