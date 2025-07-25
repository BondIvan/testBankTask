package com.testtask.bankcardmanagement.encrypt.hash;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HashCardNumber {
    private final Argon2PasswordEncoder argon2PasswordEncoder;

    public String hash(String cardNumber) {
        return argon2PasswordEncoder.encode(cardNumber);
    }

    public boolean isEquals(String cardNumber, String hash) {
        return argon2PasswordEncoder.matches(cardNumber, hash);
    }
}
