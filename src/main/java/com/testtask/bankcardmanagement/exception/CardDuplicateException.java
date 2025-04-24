package com.testtask.bankcardmanagement.exception;

public class CardDuplicateException extends RuntimeException {
    public CardDuplicateException(String message) {
        super(message);
    }
}
