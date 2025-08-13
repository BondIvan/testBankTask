package com.testtask.bankcardmanagement.model.dto.transaction;

import com.testtask.bankcardmanagement.model.enums.TransactionDirection;
import com.testtask.bankcardmanagement.model.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponse(
        BigDecimal amount,

        TransactionType type,

        TransactionDirection direction,

        Long sourceCardId,

        Long targetCardId,

        UUID transactionGroupId,

        LocalDateTime createdAt,

        String description
) { }
