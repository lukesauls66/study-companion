package com.study_companion.backend.exception.deck;

public class InvalidDeckCreationException extends DeckException {
    public InvalidDeckCreationException(String message) {
        super(message);
    }

    public InvalidDeckCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
