package com.lukesauls.studycompanion.studycompanion_backend.exception.user;

public class InvalidUserUpdateException extends RuntimeException {
    public InvalidUserUpdateException(String message) {
        super(message);
    }

    public InvalidUserUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}
