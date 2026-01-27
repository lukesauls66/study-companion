package com.study_companion.backend.exception.upload;

public class InvalidUploadCreationException extends UploadException {
    public InvalidUploadCreationException(String message) {
        super(message);
    }

    public InvalidUploadCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
