package com.lukesauls.studycompanion.studycompanion_backend.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

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
}
