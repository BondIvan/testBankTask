package com.testtask.bankcardmanagement.encrypt.hash;

import com.testtask.bankcardmanagement.exception.security.HashCardNumberException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Slf4j
@Component
public class HashCardNumber {
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final SecretKeySpec secretKeySpec;

    public HashCardNumber(@Value("${my.card.hash_card_number_key}") String key) {
        this.secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
    }

    public String hash(String cardNumber) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(cardNumber.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hmacBytes);
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed hashing card number. Wrong algorithm: {}", e.getMessage());
            throw new HashCardNumberException("Failed hashing card number. Wrong algorithm.", e);
        } catch (InvalidKeyException e) {
            log.error("Failed hashing card number. Invalid key: {}", e.getMessage());
            throw new HashCardNumberException("Failed hashing card number. Invalid key.", e);
        }
    }
}
