package com.lukesauls.studycompanion.studycompanion_backend.exception;

public class InvalidCardUpdateException extends RuntimeException {
    public InvalidCardUpdateException(String message) {
        super(message);
    }
}
