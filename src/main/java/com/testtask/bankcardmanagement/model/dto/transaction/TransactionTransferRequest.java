package com.testtask.bankcardmanagement.model.dto.transaction;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransactionTransferRequest(
        @NotNull
        @Pattern(regexp = "^\\d{16}$", message = "Invalid card number,should be - ____ ____ ____ ____")
        String fromCardNumber,

        @NotNull
        @Pattern(regexp = "^\\d{16}$", message = "Invalid card number,should be - ____ ____ ____ ____")
        String toCardNumber,

        @Positive
        @DecimalMin(value = "0.00", inclusive = false, message = "Min value 0.00")
        BigDecimal amount,

        String description
) { }
