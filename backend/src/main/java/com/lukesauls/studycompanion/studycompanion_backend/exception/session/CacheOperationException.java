package com.lukesauls.studycompanion.studycompanion_backend.exception.session;

public class CacheOperationException extends SessionException {
    public CacheOperationException(String message) {
        super(message);
    }

    public CacheOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}