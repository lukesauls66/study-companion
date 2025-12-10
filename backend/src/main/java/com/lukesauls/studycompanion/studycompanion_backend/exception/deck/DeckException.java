package com.lukesauls.studycompanion.studycompanion_backend.exception.deck;

public class DeckException extends RuntimeException {
    public DeckException(String message) {
        super(message);
    }

    public DeckException(String message, Throwable cause) {
        super(message, cause);
    }
}
