package com.lukesauls.studycompanion.studycompanion_backend.exception;

public class InvalidDeckCreationException extends RuntimeException {
    public InvalidDeckCreationException(String message) {
        super(message);
    }
}
