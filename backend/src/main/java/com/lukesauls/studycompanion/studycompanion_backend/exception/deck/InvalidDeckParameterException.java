package com.lukesauls.studycompanion.studycompanion_backend.exception.deck;

public class InvalidDeckParameterException extends RuntimeException {
    public InvalidDeckParameterException(String message) {
        super(message);
    }

    public InvalidDeckParameterException(String message, Throwable cause) {
        super(message, cause);
    }
}
