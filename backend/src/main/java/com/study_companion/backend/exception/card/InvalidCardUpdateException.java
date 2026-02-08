package com.study_companion.backend.exception.card;

public class InvalidCardUpdateException extends CardException {
    public InvalidCardUpdateException(String message) {
        super(message);
    }

    public InvalidCardUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}