package com.testtask.bankcardmanagement.model.dto.transaction;

import com.testtask.bankcardmanagement.model.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "An object containing fields by which transactions can be filtered")
public record TransactionParamFilter(
        @Schema(description = "Card id")
        Long cardId,

        @Schema(description = "Transaction type", example = "WRITE_OFF, REPLENISHMENT")
        TransactionType type,

        @Schema(description = "From this date")
        LocalDateTime fromDate,

        @Schema(description = "Until this date")
        LocalDateTime toDate,

        @Schema(description = "Check if the card belongs to an authorized user", example = "true, false")
        boolean checkOwnership
) { }
