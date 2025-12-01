package com.lukesauls.studycompanion.studycompanion_backend.exception;

public class InvalidDeckUpdateException extends RuntimeException {
    public InvalidDeckUpdateException(String message) {
        super(message);
    }
}
