package com.study_companion.backend.exception.card;

public class CardNotFoundException extends CardException {
    public CardNotFoundException(String message) {
        super(message);
    }

    public CardNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}