package com.study_companion.backend.exception.analytics;

public class AnalyticsException extends RuntimeException {
    public AnalyticsException(String message) {
        super(message);
    }

    public AnalyticsException(String message, Throwable cause) {
        super(message, cause);
    }
}
