package com.study_companion.backend.exception.analytics;

public class InvalidAnalyticsParameterException extends AnalyticsException {
    public InvalidAnalyticsParameterException(String message) {
        super(message);
    }

    public InvalidAnalyticsParameterException(String message, Throwable cause) {
        super(message, cause);
    }
}
