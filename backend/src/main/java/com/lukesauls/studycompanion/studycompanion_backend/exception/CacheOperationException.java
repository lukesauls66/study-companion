package com.lukesauls.studycompanion.studycompanion_backend.exception;

/**
 * Exception thrown when cache operations fail
 */
public class CacheOperationException extends RuntimeException {
    public CacheOperationException(String message) {
        super(message);
    }

    public CacheOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}