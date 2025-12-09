package com.lukesauls.studycompanion.studycompanion_backend.exception;

public class InvalidUserParameterException extends RuntimeException {
    public InvalidUserParameterException(String message) {
        super(message);
    }
}
