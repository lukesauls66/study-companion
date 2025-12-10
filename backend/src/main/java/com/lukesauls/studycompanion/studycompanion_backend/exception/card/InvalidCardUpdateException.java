package com.lukesauls.studycompanion.studycompanion_backend.exception.card;

public class InvalidCardUpdateException extends RuntimeException {
    public InvalidCardUpdateException(String message) {
        super(message);
    }

    public InvalidCardUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}
