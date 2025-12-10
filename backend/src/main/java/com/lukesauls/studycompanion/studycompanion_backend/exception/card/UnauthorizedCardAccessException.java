package com.lukesauls.studycompanion.studycompanion_backend.exception.card;

public class UnauthorizedCardAccessException extends RuntimeException {
    public UnauthorizedCardAccessException(String message) {
        super(message);
    }
    
    public UnauthorizedCardAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
