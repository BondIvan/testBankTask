package com.testtask.bankcardmanagement.controller.exeptionHandlers;

import com.testtask.bankcardmanagement.exception.limit.LimitExceededException;
import com.testtask.bankcardmanagement.exception.limit.LimitException;
import com.testtask.bankcardmanagement.exception.limit.LimitNotFoundException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(value = Ordered.HIGHEST_PRECEDENCE)
public class LimitExceptionHandler {
    @ExceptionHandler(LimitExceededException.class)
    public ResponseEntity<String> handleLimitExceeded(LimitExceededException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Limit exceeded:\n" + exception.getMessage());
    }

    @ExceptionHandler(LimitException.class)
    public ResponseEntity<String> handleLimit(LimitException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }

    @ExceptionHandler(LimitNotFoundException.class)
    public ResponseEntity<String> handleLimitNotFound(LimitNotFoundException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }
}
