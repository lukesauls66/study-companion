package com.lukesauls.studycompanion.studycompanion_backend.exception;

public class InvalidUserUpdateException extends RuntimeException {
    public InvalidUserUpdateException(String message) {
        super(message);
    }
}
