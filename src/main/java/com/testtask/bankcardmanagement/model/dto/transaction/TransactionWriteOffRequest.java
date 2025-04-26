package com.testtask.bankcardmanagement.model.dto.transaction;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record TransactionWriteOffRequest(
        @NotNull
        @Pattern(regexp = "^\\d{16}$")
        String fromCardNumber,

        @DecimalMin(value = "0.00", inclusive = false)
        BigDecimal amount,

        String description,

        String userEmail //TODO Temporary solution
) { }
