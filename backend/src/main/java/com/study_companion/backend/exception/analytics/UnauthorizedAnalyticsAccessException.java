package com.study_companion.backend.exception.analytics;

public class UnauthorizedAnalyticsAccessException extends AnalyticsException {
    public UnauthorizedAnalyticsAccessException(String message) {
        super(message);
    }

    public UnauthorizedAnalyticsAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}