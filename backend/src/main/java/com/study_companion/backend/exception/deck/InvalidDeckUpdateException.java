package com.study_companion.backend.exception.deck;

public class InvalidDeckUpdateException extends DeckException {
    public InvalidDeckUpdateException(String message) {
        super(message);
    }

    public InvalidDeckUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}
