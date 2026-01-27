package com.study_companion.backend.exception.card;

public class InvalidCardCreationException extends CardException {
    public InvalidCardCreationException(String message) {
        super(message);
    }

    public InvalidCardCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}