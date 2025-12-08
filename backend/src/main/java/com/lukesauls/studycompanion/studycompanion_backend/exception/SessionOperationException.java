package com.lukesauls.studycompanion.studycompanion_backend.exception;

public class SessionOperationException extends RuntimeException {
    public SessionOperationException(String message) {
        super(message);
    }

    public SessionOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}