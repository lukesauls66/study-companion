package com.lukesauls.studycompanion.studycompanion_backend.exception.card;

public class InvalidCardCreationException extends RuntimeException {
    public InvalidCardCreationException(String message) {
        super(message);
    }

    public InvalidCardCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
