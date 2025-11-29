package com.lukesauls.studycompanion.studycompanion_backend.exception;

public class InvalidPasswordChangeException extends RuntimeException {
    public InvalidPasswordChangeException(String message) {
        super(message);
    }
}
