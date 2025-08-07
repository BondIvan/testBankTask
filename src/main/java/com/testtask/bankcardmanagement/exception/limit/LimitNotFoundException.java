package com.testtask.bankcardmanagement.exception.limit;

public class LimitNotFoundException extends RuntimeException {
    public LimitNotFoundException(String message) {
        super(message);
    }
}
