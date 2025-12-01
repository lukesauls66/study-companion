package com.lukesauls.studycompanion.studycompanion_backend.exception;

public class InvalidUserCreationException extends RuntimeException {
    public InvalidUserCreationException(String message) {
        super(message);
    }
}
