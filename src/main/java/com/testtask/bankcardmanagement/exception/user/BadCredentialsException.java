package com.testtask.bankcardmanagement.exception.user;

public class BadCredentialsException extends RuntimeException {
    public BadCredentialsException(String message) {
        super(message);
    }
}
