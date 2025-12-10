package com.lukesauls.studycompanion.studycompanion_backend.exception.deck;

public class InvalidDeckUpdateException extends RuntimeException {
    public InvalidDeckUpdateException(String message) {
        super(message);
    }

    public InvalidDeckUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}
