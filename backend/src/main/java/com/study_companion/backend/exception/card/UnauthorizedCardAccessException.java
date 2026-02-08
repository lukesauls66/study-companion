package com.study_companion.backend.exception.card;

public class UnauthorizedCardAccessException extends CardException {
    public UnauthorizedCardAccessException(String message) {
        super(message);
    }

    public UnauthorizedCardAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}