package com.lukesauls.studycompanion.studycompanion_backend.exception.deck;

public class InvalidDeckCreationException extends RuntimeException {
    public InvalidDeckCreationException(String message) {
        super(message);
    }

    public InvalidDeckCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
