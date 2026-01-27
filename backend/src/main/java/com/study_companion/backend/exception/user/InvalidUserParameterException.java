package com.study_companion.backend.exception.user;

public class InvalidUserParameterException extends UserException {
    public InvalidUserParameterException(String message) {
        super(message);
    }

    public InvalidUserParameterException(String message, Throwable cause) {
        super(message, cause);
    }
}
