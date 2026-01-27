package com.study_companion.backend.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.study_companion.backend.exception.card.CardNotFoundException;
import com.study_companion.backend.exception.card.CardOperationException;
import com.study_companion.backend.exception.card.InvalidCardCreationException;
import com.study_companion.backend.exception.card.InvalidCardParameterException;
import com.study_companion.backend.exception.card.InvalidCardUpdateException;
import com.study_companion.backend.exception.card.UnauthorizedCardAccessException;
import com.study_companion.backend.exception.deck.DeckNotFoundException;
import com.study_companion.backend.exception.deck.DeckOperationException;
import com.study_companion.backend.exception.deck.InvalidDeckCreationException;
import com.study_companion.backend.exception.deck.InvalidDeckParameterException;
import com.study_companion.backend.exception.deck.InvalidDeckUpdateException;
import com.study_companion.backend.exception.deck.UnauthorizedDeckAccessException;
import com.study_companion.backend.exception.session.CacheOperationException;
import com.study_companion.backend.exception.session.InvalidSessionParameterException;
import com.study_companion.backend.exception.session.SessionOperationException;
import com.study_companion.backend.exception.upload.InvalidUploadCreationException;
import com.study_companion.backend.exception.upload.InvalidUploadParameterException;
import com.study_companion.backend.exception.upload.UnauthorizedUploadAccessException;
import com.study_companion.backend.exception.upload.UploadNotFoundException;
import com.study_companion.backend.exception.upload.UploadOperationException;
import com.study_companion.backend.exception.user.InvalidPasswordChangeException;
import com.study_companion.backend.exception.user.InvalidUserCreationException;
import com.study_companion.backend.exception.user.InvalidUserParameterException;
import com.study_companion.backend.exception.user.InvalidUserUpdateException;
import com.study_companion.backend.exception.user.UserAlreadyExistsException;
import com.study_companion.backend.exception.user.UserNotFoundException;
import com.study_companion.backend.exception.user.UserOperationException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<String> handleUserAlreadyExists(UserAlreadyExistsException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFound(UserNotFoundException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(InvalidPasswordChangeException.class)
    public ResponseEntity<String> handleInvalidPasswordChange(InvalidPasswordChangeException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(InvalidUserCreationException.class)
    public ResponseEntity<String> handleInvalidUserCreation(InvalidUserCreationException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(InvalidUserUpdateException.class)
    public ResponseEntity<String> handleInvalidUserUpdate(InvalidUserUpdateException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(DeckNotFoundException.class)
    public ResponseEntity<String> handleDeckNotFound(DeckNotFoundException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(UnauthorizedDeckAccessException.class)
    public ResponseEntity<String> handleUnauthorizedDeckAccess(UnauthorizedDeckAccessException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(InvalidDeckCreationException.class)
    public ResponseEntity<String> handleInvalidDeckCreation(InvalidDeckCreationException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(InvalidDeckUpdateException.class)
    public ResponseEntity<String> handleInvalidDeckUpdate(InvalidDeckUpdateException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(InvalidCardUpdateException.class)
    public ResponseEntity<String> handleInvalidCardUpdate(InvalidCardUpdateException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(InvalidCardCreationException.class)
    public ResponseEntity<String> handleInvalidCardCreation(InvalidCardCreationException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(CardNotFoundException.class)
    public ResponseEntity<String> handleCardNotFound(CardNotFoundException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(UnauthorizedCardAccessException.class)
    public ResponseEntity<String> handleUnauthorizedCardAccess(UnauthorizedCardAccessException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(UnauthorizedUploadAccessException.class)
    public ResponseEntity<String> handleUnauthorizedUploadAccess(UnauthorizedUploadAccessException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(InvalidUploadCreationException.class)
    public ResponseEntity<String> handleInvalidUploadCreation(InvalidUploadCreationException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(UploadNotFoundException.class)
    public ResponseEntity<String> handleUploadNotFound(UploadNotFoundException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(InvalidSessionParameterException.class)
    public ResponseEntity<String> handleInvalidSessionParameter(InvalidSessionParameterException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(InvalidUserParameterException.class)
    public ResponseEntity<String> handleInvalidUserParameter(InvalidUserParameterException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(InvalidDeckParameterException.class)
    public ResponseEntity<String> handleInvalidDeckParameter(InvalidDeckParameterException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(InvalidCardParameterException.class)
    public ResponseEntity<String> handleInvalidCardParameter(InvalidCardParameterException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(InvalidUploadParameterException.class)
    public ResponseEntity<String> handleInvalidUploadParameter(InvalidUploadParameterException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler({SessionOperationException.class, CacheOperationException.class, UserOperationException.class, DeckOperationException.class, CardOperationException.class, UploadOperationException.class})
    public ResponseEntity<String> handleSystemOperations(RuntimeException e) {
        return ResponseEntity.badRequest().body("Internal server error");
    }
}
