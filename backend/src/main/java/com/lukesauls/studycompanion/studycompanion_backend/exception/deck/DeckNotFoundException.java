package com.lukesauls.studycompanion.studycompanion_backend.exception.deck;

public class DeckNotFoundException extends RuntimeException {
    public DeckNotFoundException(String message) {
        super(message);
    }

    public DeckNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
