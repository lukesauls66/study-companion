package com.lukesauls.studycompanion.studycompanion_backend.exception;

public class UnauthorizedUploadAccessException extends RuntimeException {
    public UnauthorizedUploadAccessException(String message) {
        super(message);
    }
}
