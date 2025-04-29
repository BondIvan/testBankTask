package com.testtask.bankcardmanagement.model.dto.transaction;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Request object for funds debit")
public record TransactionWriteOffRequest(
        @Schema(description = "The card number from which funds will be debited", example = "**** **** **** 1234")
        @NotNull
        @Pattern(regexp = "^\\d{16}$", message = "Invalid card number,should be - ____ ____ ____ ____")
        String fromCardNumber,

        @Schema(description = "The amount of funds involved in the transaction", example = "111")
        @Positive
        @DecimalMin(value = "0.00", inclusive = false, message = "Min value 0.00")
        BigDecimal amount,

        @Schema(description = "Transaction Description", example = "Some text")
        String description
) { }
