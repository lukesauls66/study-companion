package com.lukesauls.studycompanion.studycompanion_backend.exception.user;

public class InvalidUserCreationException extends RuntimeException {
    public InvalidUserCreationException(String message) {
        super(message);
    }

    public InvalidUserCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
