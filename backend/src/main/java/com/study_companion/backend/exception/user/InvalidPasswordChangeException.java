package com.study_companion.backend.exception.user;

public class InvalidPasswordChangeException extends UserException {
    public InvalidPasswordChangeException(String message) {
        super(message);
    }

    public InvalidPasswordChangeException(String message, Throwable cause) {
        super(message, cause);
    }
}
