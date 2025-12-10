package com.lukesauls.studycompanion.studycompanion_backend.exception.upload;

public class InvalidUploadCreationException extends RuntimeException {
    public InvalidUploadCreationException(String message) {
        super(message);
    }

    public InvalidUploadCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
