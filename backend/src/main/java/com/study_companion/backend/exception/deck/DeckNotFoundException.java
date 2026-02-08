package com.study_companion.backend.exception.deck;

public class DeckNotFoundException extends DeckException {
    public DeckNotFoundException(String message) {
        super(message);
    }

    public DeckNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
