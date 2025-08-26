package com.testtask.bankcardmanagement.model.dto.transaction;

import com.testtask.bankcardmanagement.model.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentTransactionResponse(
        BigDecimal amount,

        TransactionType type,

        Long sourceCardId,

        Long targetCardId,

        UUID transactionGroupId,

        LocalDateTime createdAt,

        String description
) { }
