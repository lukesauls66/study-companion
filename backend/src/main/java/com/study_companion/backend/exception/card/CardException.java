package com.study_companion.backend.exception.card;

public class CardException extends RuntimeException {
    public CardException(String message) {
        super(message);
    }

    public CardException(String message, Throwable cause) {
        super(message, cause);
    }
}
