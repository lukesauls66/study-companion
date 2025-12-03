package com.lukesauls.studycompanion.studycompanion_backend.exception;

public class CardNotFoundException extends RuntimeException {
    public CardNotFoundException(String message) {
        super(message);
    }
}
