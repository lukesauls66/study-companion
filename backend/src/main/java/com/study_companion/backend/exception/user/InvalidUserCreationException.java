package com.study_companion.backend.exception.user;

public class InvalidUserCreationException extends UserException {
    public InvalidUserCreationException(String message) {
        super(message);
    }

    public InvalidUserCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
