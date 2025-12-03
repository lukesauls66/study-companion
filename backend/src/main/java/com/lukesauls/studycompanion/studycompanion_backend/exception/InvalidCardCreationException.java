package com.lukesauls.studycompanion.studycompanion_backend.exception;

public class InvalidCardCreationException extends RuntimeException {
    public InvalidCardCreationException(String message) {
        super(message);
    }
}
