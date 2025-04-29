package com.testtask.bankcardmanagement.model.dto.transaction;

import com.testtask.bankcardmanagement.model.dto.card.CardResponse;
import com.testtask.bankcardmanagement.model.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Transaction response object")
public record TransactionResponse(
        @Schema(description = "The amount of funds involved in the transaction", example = "111")
        BigDecimal amount,

        @Schema(description = "Transaction type", example = "WRITE_OFF, REPLENISHMENT")
        TransactionType type,

        @Schema(description = "Card response object")
        CardResponse card,

        @Schema(description = "Masked target card number", example = "**** **** **** 1234")
        String targetCard,

        @Schema(description = "Date of the operation")
        LocalDateTime datetime,

        @Schema(description = "Transaction Description", example = "Some text")
        String description
) { }
