package com.study_companion.backend.exception.user;

public class InvalidUserUpdateException extends UserException {
    public InvalidUserUpdateException(String message) {
        super(message);
    }

    public InvalidUserUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}
