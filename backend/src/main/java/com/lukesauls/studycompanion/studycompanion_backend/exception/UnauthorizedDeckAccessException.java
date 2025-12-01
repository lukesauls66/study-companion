package com.lukesauls.studycompanion.studycompanion_backend.exception;

public class UnauthorizedDeckAccessException extends RuntimeException {
    public UnauthorizedDeckAccessException(String message) {
        super(message);
    }
}
