package com.lukesauls.studycompanion.studycompanion_backend.exception.user;

public class InvalidUserParameterException extends RuntimeException {
    public InvalidUserParameterException(String message) {
        super(message);
    }

    public InvalidUserParameterException(String message, Throwable cause) {
        super(message, cause);
    }
}
