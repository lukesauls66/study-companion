package com.study_companion.backend.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.study_companion.backend.dto.GenericDto.ErrorResponse;
import com.study_companion.backend.exception.analytics.AnalyticsException;
import com.study_companion.backend.exception.analytics.AnalyticsOperationException;
import com.study_companion.backend.exception.analytics.InvalidAnalyticsParameterException;
import com.study_companion.backend.exception.analytics.UnauthorizedAnalyticsAccessException;
import com.study_companion.backend.exception.card.CardException;
import com.study_companion.backend.exception.card.CardNotFoundException;
import com.study_companion.backend.exception.card.CardOperationException;
import com.study_companion.backend.exception.card.InvalidCardCreationException;
import com.study_companion.backend.exception.card.InvalidCardParameterException;
import com.study_companion.backend.exception.card.InvalidCardUpdateException;
import com.study_companion.backend.exception.card.UnauthorizedCardAccessException;
import com.study_companion.backend.exception.deck.DeckException;
import com.study_companion.backend.exception.deck.DeckNotFoundException;
import com.study_companion.backend.exception.deck.DeckOperationException;
import com.study_companion.backend.exception.deck.InvalidDeckCreationException;
import com.study_companion.backend.exception.deck.InvalidDeckParameterException;
import com.study_companion.backend.exception.deck.InvalidDeckUpdateException;
import com.study_companion.backend.exception.deck.UnauthorizedDeckAccessException;
import com.study_companion.backend.exception.session.CacheOperationException;
import com.study_companion.backend.exception.session.InvalidSessionParameterException;
import com.study_companion.backend.exception.session.SessionException;
import com.study_companion.backend.exception.session.SessionOperationException;
import com.study_companion.backend.exception.upload.InvalidUploadCreationException;
import com.study_companion.backend.exception.upload.InvalidUploadParameterException;
import com.study_companion.backend.exception.upload.UnauthorizedUploadAccessException;
import com.study_companion.backend.exception.upload.UploadException;
import com.study_companion.backend.exception.upload.UploadNotFoundException;
import com.study_companion.backend.exception.upload.UploadOperationException;
import com.study_companion.backend.exception.user.InvalidPasswordChangeException;
import com.study_companion.backend.exception.user.InvalidUserCreationException;
import com.study_companion.backend.exception.user.InvalidUserParameterException;
import com.study_companion.backend.exception.user.InvalidUserUpdateException;
import com.study_companion.backend.exception.user.UnauthorizedUserAccessException;
import com.study_companion.backend.exception.user.UserAlreadyExistsException;
import com.study_companion.backend.exception.user.UserException;
import com.study_companion.backend.exception.user.UserNotFoundException;
import com.study_companion.backend.exception.user.UserOperationException;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 500 - System / Operation Failures
    @ExceptionHandler({ SessionOperationException.class, CacheOperationException.class, UserOperationException.class,
            DeckOperationException.class, CardOperationException.class, UploadOperationException.class,
            AnalyticsOperationException.class })
    public ResponseEntity<ErrorResponse> handleSystemOperations(RuntimeException e) {
        return ResponseEntity.internalServerError()
                .body(new ErrorResponse("A system error occurred. Please try again later.", true));
    }

    // 400 - Bad Request (validation / invalid input)
    @ExceptionHandler({ UserException.class, InvalidPasswordChangeException.class, InvalidUserCreationException.class,
            InvalidUserUpdateException.class, InvalidUserParameterException.class, DeckException.class,
            InvalidDeckCreationException.class, InvalidDeckUpdateException.class, InvalidDeckParameterException.class,
            CardException.class, InvalidCardUpdateException.class, InvalidCardCreationException.class,
            InvalidCardParameterException.class, UploadException.class,
            InvalidUploadCreationException.class, InvalidUploadParameterException.class, SessionException.class,
            InvalidSessionParameterException.class, AnalyticsException.class,
            InvalidAnalyticsParameterException.class })
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), true));
    }

    // 401 - Unauthorized
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException e) {
        return ResponseEntity.status(401)
                .body(new ErrorResponse("Invalid credentials", true));
    }

    // 403 - Forbidden
    @ExceptionHandler({ UnauthorizedUserAccessException.class, UnauthorizedDeckAccessException.class,
            UnauthorizedCardAccessException.class, UnauthorizedUploadAccessException.class,
            UnauthorizedAnalyticsAccessException.class })
    public ResponseEntity<ErrorResponse> handleForbidden(RuntimeException e) {
        return ResponseEntity.status(403).body(new ErrorResponse(e.getMessage(), true));
    }

    // 404 - Not Found
    @ExceptionHandler({ UserNotFoundException.class, DeckNotFoundException.class, CardNotFoundException.class,
            UploadNotFoundException.class })
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException e) {
        return ResponseEntity.status(404).body(new ErrorResponse(e.getMessage(), true));
    }

    // 409 - Conflict
    @ExceptionHandler({ UserAlreadyExistsException.class })
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException e) {
        return ResponseEntity.status(409).body(new ErrorResponse(e.getMessage(), true));
    }

    // 500 - Generic fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        System.err.println("Unhandled exception: " + e.getClass().getName() + " - " + e.getMessage());
        e.printStackTrace();

        return ResponseEntity.internalServerError()
                .body(new ErrorResponse("An unexpected error occurred. Please try again later.", true));
    }
}
