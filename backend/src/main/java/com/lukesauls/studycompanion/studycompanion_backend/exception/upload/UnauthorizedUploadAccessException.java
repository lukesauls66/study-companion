package com.lukesauls.studycompanion.studycompanion_backend.exception.upload;

public class UnauthorizedUploadAccessException extends RuntimeException {
    public UnauthorizedUploadAccessException(String message) {
        super(message);
    }

    public UnauthorizedUploadAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
