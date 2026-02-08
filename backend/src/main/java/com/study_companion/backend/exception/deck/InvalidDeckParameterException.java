package com.study_companion.backend.exception.deck;

public class InvalidDeckParameterException extends DeckException {
    public InvalidDeckParameterException(String message) {
        super(message);
    }

    public InvalidDeckParameterException(String message, Throwable cause) {
        super(message, cause);
    }
}
