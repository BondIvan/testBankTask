package com.testtask.bankcardmanagement.model.dto.transaction;

import com.testtask.bankcardmanagement.model.enums.TransactionType;

import java.time.LocalDateTime;

public record TransactionParamFilter(
        Long cardId,
        TransactionType type,
        LocalDateTime fromDate,
        LocalDateTime toDate,
        boolean checkOwnership
) { }
