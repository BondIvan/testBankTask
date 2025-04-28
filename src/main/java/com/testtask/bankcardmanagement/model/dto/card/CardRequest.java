package com.testtask.bankcardmanagement.model.dto.card;

import com.testtask.bankcardmanagement.model.dto.limit.LimitRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.List;

public record CardRequest(
        @NotNull
        @Pattern(regexp = "^\\d{16}$")
        String cardNumber,

        @Future(message = "The expiration date must be in future")
        LocalDate expirationDate,

        @NotNull
        @Email(message = "Invalid email format")
        String ownerEmail,

        @Valid
        List<LimitRequest> limits
) { }
