package com.study_companion.backend.exception.upload;

public class InvalidUploadParameterException extends UploadException {
    public InvalidUploadParameterException(String message) {
        super(message);
    }

    public InvalidUploadParameterException(String message, Throwable cause) {
        super(message, cause);
    }
}
