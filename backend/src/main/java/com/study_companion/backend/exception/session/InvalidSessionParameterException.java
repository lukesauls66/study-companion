package com.study_companion.backend.exception.session;

public class InvalidSessionParameterException extends SessionException {
    public InvalidSessionParameterException(String message) {
        super(message);
    }

    public InvalidSessionParameterException(String message, Throwable cause) {
        super(message, cause);
    }
}
