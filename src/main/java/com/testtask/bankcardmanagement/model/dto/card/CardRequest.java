package com.testtask.bankcardmanagement.model.dto.card;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record CardRequest(
        @NotNull
        @Pattern(regexp = "^\\d{16}$")
        String cardNumber,

        @Future(message = "The expiration date must be in future")
        LocalDate expirationDate,

        @NotNull
        String ownerEmail
) { }
