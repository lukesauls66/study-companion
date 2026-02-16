package com.study_companion.backend.exception.user;

public class UnauthorizedUserAccessException extends UserException {
    public UnauthorizedUserAccessException(String message) {
        super(message);
    }

    public UnauthorizedUserAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
