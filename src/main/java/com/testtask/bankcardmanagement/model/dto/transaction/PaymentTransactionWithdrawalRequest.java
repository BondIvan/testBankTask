package com.testtask.bankcardmanagement.model.dto.transaction;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PaymentTransactionWithdrawalRequest(
        @NotNull
        @Pattern(regexp = "^\\d{16}$", message = "Invalid card number,should be - ____ ____ ____ ____")
        String fromCardNumber,

        @Positive
        @DecimalMin(value = "1.00", inclusive = false, message = "Min value 1.00")
        BigDecimal amount,

        String description
) { }
