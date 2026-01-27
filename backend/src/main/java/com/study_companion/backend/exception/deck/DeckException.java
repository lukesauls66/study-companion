package com.study_companion.backend.exception.deck;

public class DeckException extends RuntimeException {
    public DeckException(String message) {
        super(message);
    }

    public DeckException(String message, Throwable cause) {
        super(message, cause);
    }
}
