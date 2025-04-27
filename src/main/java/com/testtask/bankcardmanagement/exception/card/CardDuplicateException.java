package com.testtask.bankcardmanagement.exception.card;

public class CardDuplicateException extends RuntimeException {
    public CardDuplicateException(String message) {
        super(message);
    }
}
