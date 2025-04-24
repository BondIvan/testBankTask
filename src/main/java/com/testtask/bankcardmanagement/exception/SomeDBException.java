package com.testtask.bankcardmanagement.exception;

public class SomeDBException extends RuntimeException {
    public SomeDBException(String message, Throwable cause) {
        super(message, cause);
    }
}
