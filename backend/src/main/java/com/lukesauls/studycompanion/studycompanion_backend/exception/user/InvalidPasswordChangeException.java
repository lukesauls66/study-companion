package com.lukesauls.studycompanion.studycompanion_backend.exception.user;

public class InvalidPasswordChangeException extends RuntimeException {
    public InvalidPasswordChangeException(String message) {
        super(message);
    }
    public InvalidPasswordChangeException(String message, Throwable cause) {
        super(message, cause);
    }
}
