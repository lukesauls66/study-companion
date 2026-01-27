package com.study_companion.backend.exception.card;

public class InvalidCardParameterException extends CardException {
    public InvalidCardParameterException(String message) {
        super(message);
    }

    public InvalidCardParameterException(String message, Throwable cause) {
        super(message, cause);
    }
}