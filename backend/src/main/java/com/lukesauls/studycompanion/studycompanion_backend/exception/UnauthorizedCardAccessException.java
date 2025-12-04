package com.lukesauls.studycompanion.studycompanion_backend.exception;

public class UnauthorizedCardAccessException extends RuntimeException {
    public UnauthorizedCardAccessException(String message) {
        super(message);
    }
}
