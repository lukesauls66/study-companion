package com.study_companion.backend.exception.deck;

public class UnauthorizedDeckAccessException extends DeckException {
    public UnauthorizedDeckAccessException(String message) {
        super(message);
    }

    public UnauthorizedDeckAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
