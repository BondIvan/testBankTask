package com.testtask.bankcardmanagement.exception.card;

public class CardNotAvailableException extends RuntimeException {
    public CardNotAvailableException(String message) {
        super(message);
    }
}
