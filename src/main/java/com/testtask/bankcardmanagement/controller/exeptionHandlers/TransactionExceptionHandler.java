package com.testtask.bankcardmanagement.controller.exeptionHandlers;

import com.testtask.bankcardmanagement.exception.transaction.TransactionDeclinedException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class TransactionExceptionHandler {
    @ExceptionHandler(TransactionDeclinedException.class)
    public ResponseEntity<String> handleTransactionDeclined(TransactionDeclinedException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Trouble with making transaction:\n" + exception.getMessage());
    }
}
