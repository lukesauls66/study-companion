package com.study_companion.backend.exception.upload;

public class UploadNotFoundException extends UploadException {
    public UploadNotFoundException(String message) {
        super(message);
    }

    public UploadNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
