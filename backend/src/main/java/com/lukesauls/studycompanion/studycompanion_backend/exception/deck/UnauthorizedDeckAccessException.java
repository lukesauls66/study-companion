package com.lukesauls.studycompanion.studycompanion_backend.exception.deck;

public class UnauthorizedDeckAccessException extends RuntimeException {
    public UnauthorizedDeckAccessException(String message) {
        super(message);
    }

    public UnauthorizedDeckAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
