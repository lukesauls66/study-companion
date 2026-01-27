package com.study_companion.backend.exception.session;

public class CacheOperationException extends SessionException {
    public CacheOperationException(String message) {
        super(message);
    }

    public CacheOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
