package com.testtask.bankcardmanagement.encrypt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Component
public class AESEncryption {
    private static final String ENCRYPT_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private final SecretKeySpec secretKeySpec;

    public AESEncryption(@Value("${my.encrypt_key}") String key) {
        byte[] bytes = key.getBytes();
        this.secretKeySpec = new SecretKeySpec(bytes, "AES");
    }

    public String encrypt(String data) {
        byte[] iv = generateIV();

        byte[] encrypted;
        byte[] concatenatedIvAndEncrypted;
        try {
            // Create an AES Cipher instance
            Cipher cipher = Cipher.getInstance(ENCRYPT_ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, gcmSpec);

            // Concatenation of IV and encrypted data
            encrypted = cipher.doFinal(data.getBytes());
            concatenatedIvAndEncrypted = new byte[iv.length + encrypted.length]; // arr2
// Array source (iv) | what position to start in source array (iv) | where to copy (arr2) | from what position to start insertion in target array (arr2)
// | number of elements to insert
            System.arraycopy(iv, 0, concatenatedIvAndEncrypted, 0, iv.length);
            System.arraycopy(encrypted, 0, concatenatedIvAndEncrypted, iv.length, encrypted.length);
        } catch (Exception e) {
            throw new RuntimeException("Encryption error: " + e.getMessage());
        }

        String base64View = Base64.getEncoder().encodeToString(concatenatedIvAndEncrypted);

        // Clearing sensitive data from memory
        Arrays.fill(iv, (byte) '\0');
        Arrays.fill(encrypted, (byte) '\0');
        Arrays.fill(concatenatedIvAndEncrypted, (byte) '\0');

        return base64View;
    }

    public String decrypt(String encryptedData) {
        byte[] fromBase64ToByteView = Base64.getDecoder().decode(encryptedData);
        byte[] iv = Arrays.copyOfRange(fromBase64ToByteView, 0, GCM_IV_LENGTH);
        byte[] encryptText = Arrays.copyOfRange(fromBase64ToByteView, GCM_IV_LENGTH, fromBase64ToByteView.length);

        byte[] decrypted;
        try {
            Cipher cipher = Cipher.getInstance(ENCRYPT_ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcmSpec);
            decrypted = cipher.doFinal(encryptText);
        } catch (Exception e) {
            throw new RuntimeException("Decryption error: " + e.getMessage());
        }

        // Clearing sensitive data from memory
        Arrays.fill(fromBase64ToByteView, (byte) '\0');
        Arrays.fill(iv, (byte) '\0');
        Arrays.fill(encryptText, (byte) '\0');

        return new String(decrypted);
    }

    private byte[] generateIV() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] iv = new byte[GCM_IV_LENGTH];
        secureRandom.nextBytes(iv);

        return iv;
    }

}
