package com.testtask.bankcardmanagement.model.dto.card;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record CreateCardRequest(
        @NotNull
        @Pattern(regexp = "^\\d{16}$", message = "Invalid card number,should be - ____ ____ ____ ____")
        String cardNumber,

        @Future(message = "The expiration date must be in future")
        LocalDate expirationDate,

        @NotNull
        @Email(message = "Invalid email format")
        String ownerEmail
) { }
