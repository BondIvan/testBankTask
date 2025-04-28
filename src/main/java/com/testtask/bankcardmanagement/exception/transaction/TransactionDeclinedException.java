package com.testtask.bankcardmanagement.exception.transaction;

public class TransactionDeclinedException extends RuntimeException {
    public TransactionDeclinedException(String message) {
        super(message);
    }
}
