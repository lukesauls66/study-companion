package com.lukesauls.studycompanion.studycompanion_backend.exception;

public class DeckOperationException extends RuntimeException {
    public DeckOperationException(String message) {
        super(message);
    }    

    public DeckOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
