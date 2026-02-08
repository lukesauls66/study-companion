package com.study_companion.backend.exception.deck;

public class DeckOperationException extends DeckException {
    public DeckOperationException(String message) {
        super(message);
    }

    public DeckOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
