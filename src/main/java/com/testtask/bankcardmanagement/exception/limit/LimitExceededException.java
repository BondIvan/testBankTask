package com.testtask.bankcardmanagement.exception.limit;

public class LimitExceededException extends RuntimeException {
    public LimitExceededException(String message) {
        super(message);
    }
}
