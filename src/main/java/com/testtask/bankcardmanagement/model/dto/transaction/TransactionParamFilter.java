package com.testtask.bankcardmanagement.model.dto.transaction;

import com.testtask.bankcardmanagement.model.enums.TransactionType;

import java.time.Instant;
import java.time.LocalDateTime;

public record TransactionParamFilter(
        String userEmail,
// TODO Add new filters here
        TransactionType type,

        Instant fromDate,

        Instant toDate
) { }
