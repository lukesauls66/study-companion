package com.lukesauls.studycompanion.studycompanion_backend.exception;

public class UploadNotFoundException extends RuntimeException {
    public UploadNotFoundException(String message) {
        super(message);
    }
}
