package com.lukesauls.studycompanion.studycompanion_backend.exception.upload;

public class UploadOperationException extends UploadException {
    public UploadOperationException(String message) {
        super(message);
    }

    public UploadOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
