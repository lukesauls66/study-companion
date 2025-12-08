package com.lukesauls.studycompanion.studycompanion_backend.exception;

/**
 * Exception thrown when invalid session parameters are provided
 */
public class InvalidSessionParameterException extends RuntimeException {
    public InvalidSessionParameterException(String message) {
        super(message);
    }

    public InvalidSessionParameterException(String message, Throwable cause) {
        super(message, cause);
    }
}