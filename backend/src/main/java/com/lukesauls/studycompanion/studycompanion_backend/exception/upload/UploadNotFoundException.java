package com.lukesauls.studycompanion.studycompanion_backend.exception.upload;

public class UploadNotFoundException extends RuntimeException {
    public UploadNotFoundException(String message) {
        super(message);
    }

    public UploadNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
