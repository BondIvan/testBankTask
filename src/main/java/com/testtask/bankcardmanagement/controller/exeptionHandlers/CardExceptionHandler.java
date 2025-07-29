package com.testtask.bankcardmanagement.controller.exeptionHandlers;

import com.testtask.bankcardmanagement.exception.card.CardBalanceException;
import com.testtask.bankcardmanagement.exception.card.CardDuplicateException;
import com.testtask.bankcardmanagement.exception.card.CardNotAvailableException;
import com.testtask.bankcardmanagement.exception.card.CardNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CardExceptionHandler {
    @ExceptionHandler(CardBalanceException.class)
    public ResponseEntity<String> handleCardBalance(CardBalanceException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Card balance trouble:\n" + exception.getMessage());
    }

    @ExceptionHandler(CardDuplicateException.class)
    public ResponseEntity<String> handleCardDuplicate(CardDuplicateException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Card has duplicate:\n" + exception.getMessage());
    }

    @ExceptionHandler(CardNotAvailableException.class)
    public ResponseEntity<String> handleCardNotAvailable(CardNotAvailableException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Card status is not 'active':\n" + exception.getMessage());
    }

    @ExceptionHandler(CardNotFoundException.class)
    public ResponseEntity<String> handleCardNotFound(CardNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("There is no card:\n" + exception.getMessage());
    }
}
