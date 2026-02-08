package com.study_companion.backend.exception.user;

public class UserOperationException extends UserException {
    public UserOperationException(String message) {
        super(message);
    }

    public UserOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
