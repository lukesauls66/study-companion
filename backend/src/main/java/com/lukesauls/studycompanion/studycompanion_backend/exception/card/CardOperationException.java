package com.lukesauls.studycompanion.studycompanion_backend.exception.card;

public class CardOperationException extends CardException {
    public CardOperationException(String message) {
        super(message);
    }

    public CardOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
