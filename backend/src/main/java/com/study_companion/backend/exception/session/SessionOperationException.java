package com.study_companion.backend.exception.session;

public class SessionOperationException extends SessionException {
    public SessionOperationException(String message) {
        super(message);
    }

    public SessionOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
