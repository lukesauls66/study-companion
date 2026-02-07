package com.study_companion.backend.exception.analytics;

public class AnalyticsOperationException extends AnalyticsException {
    public AnalyticsOperationException(String message) {
        super(message);
    }

    public AnalyticsOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
