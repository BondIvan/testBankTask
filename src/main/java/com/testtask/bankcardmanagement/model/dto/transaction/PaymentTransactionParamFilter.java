package com.testtask.bankcardmanagement.model.dto.transaction;

import java.time.Instant;

public record PaymentTransactionParamFilter(
        String userEmail,
// TODO Add new filters here
        String type,

        Instant fromDate,

        Instant toDate
) { }
