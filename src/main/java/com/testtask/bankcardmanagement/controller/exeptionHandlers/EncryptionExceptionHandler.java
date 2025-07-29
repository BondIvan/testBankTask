package com.testtask.bankcardmanagement.controller.exeptionHandlers;

import com.testtask.bankcardmanagement.exception.encryption.AESEncryptionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class EncryptionExceptionHandler {
    @ExceptionHandler(AESEncryptionException.class)
    public ResponseEntity<String> handleAESEncryption(AESEncryptionException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Trouble with card number encryption:\n" + exception.getMessage());
    }
}
