package com.study_companion.backend.exception.upload;

public class UnauthorizedUploadAccessException extends UploadException {
    public UnauthorizedUploadAccessException(String message) {
        super(message);
    }

    public UnauthorizedUploadAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
