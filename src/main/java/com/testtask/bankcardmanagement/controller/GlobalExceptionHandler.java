package com.testtask.bankcardmanagement.controller;

import com.testtask.bankcardmanagement.exception.card.CardBalanceException;
import com.testtask.bankcardmanagement.exception.card.CardDuplicateException;
import com.testtask.bankcardmanagement.exception.card.CardNotAvailableException;
import com.testtask.bankcardmanagement.exception.card.CardNotFoundException;
import com.testtask.bankcardmanagement.exception.db.SomeDBException;
import com.testtask.bankcardmanagement.exception.encryption.AESEncryptionException;
import com.testtask.bankcardmanagement.exception.limit.LimitExceededException;
import com.testtask.bankcardmanagement.exception.limit.LimitException;
import com.testtask.bankcardmanagement.exception.other.ConvertingEnumException;
import com.testtask.bankcardmanagement.exception.other.InvalidSortFieldException;
import com.testtask.bankcardmanagement.exception.security.AccessDeniedException;
import com.testtask.bankcardmanagement.exception.security.JwtTokenException;
import com.testtask.bankcardmanagement.exception.transaction.TransactionDeclinedException;
import com.testtask.bankcardmanagement.exception.user.UserDuplicateException;
import com.testtask.bankcardmanagement.exception.user.UserNotFoundException;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Hidden
public class GlobalExceptionHandler {

// ALL

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAll(Exception exception) {
        return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body(exception.getMessage());
    }

// Security
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDenied(AccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(exception.getMessage());
    }

    @ExceptionHandler(JwtTokenException.class)
    public ResponseEntity<String> handleJwtToken(JwtTokenException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(exception.getMessage());
    }

// Validation

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getBindingResult().getAllErrors().stream()
                .collect(Collectors.toMap(
                        error -> ((FieldError) error).getField(),
                        error -> error.getDefaultMessage(),
                        (existing, replacement) -> replacement,
                        HashMap::new
                )));
    }

// Card
    @ExceptionHandler(CardBalanceException.class)
    public ResponseEntity<String> handleCardBalance(CardBalanceException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }

    @ExceptionHandler(CardDuplicateException.class)
    public ResponseEntity<String> handleCardDuplicate(CardDuplicateException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }

    @ExceptionHandler(CardNotAvailableException.class)
    public ResponseEntity<String> handleCardNotAvailable(CardNotAvailableException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }

    @ExceptionHandler(CardNotFoundException.class)
    public ResponseEntity<String> handleCardNotFound(CardNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

// User

    @ExceptionHandler(UserDuplicateException.class)
    public ResponseEntity<String> handleUserDuplicate(UserDuplicateException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFound(UserNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

// DB

    @ExceptionHandler(SomeDBException.class)
    public ResponseEntity<String> handleSomeDB(SomeDBException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
    }

// Encryption

    @ExceptionHandler(AESEncryptionException.class)
    public ResponseEntity<String> handleAESEncryption(AESEncryptionException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
    }

// Limit

    @ExceptionHandler(LimitExceededException.class)
    public ResponseEntity<String> handleLimitExceeded(LimitExceededException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }

    @ExceptionHandler(LimitException.class)
    public ResponseEntity<String> handleLimit(LimitException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }

// Transaction

    @ExceptionHandler(TransactionDeclinedException.class)
    public ResponseEntity<String> handleTransactionDeclined(TransactionDeclinedException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }

// Other

    @ExceptionHandler(ConvertingEnumException.class)
    public ResponseEntity<String> handleConvertingEnum(ConvertingEnumException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
    }

    @ExceptionHandler(InvalidSortFieldException.class)
    public ResponseEntity<String> handleInvalidSortField(InvalidSortFieldException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }

}
