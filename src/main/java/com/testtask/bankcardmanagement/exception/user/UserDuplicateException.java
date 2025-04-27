package com.testtask.bankcardmanagement.exception.user;

public class UserDuplicateException extends RuntimeException {
    public UserDuplicateException(String message) {
        super(message);
    }
}
