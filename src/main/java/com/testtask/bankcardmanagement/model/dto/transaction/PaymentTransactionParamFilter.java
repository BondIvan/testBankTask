package com.testtask.bankcardmanagement.model.dto.transaction;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;

public record PaymentTransactionParamFilter(
        Long userId,

        String type,

        BigDecimal fromAmount,

        BigDecimal toAmount,

        LocalDate fromDate,

        LocalDate toDate
) {

    public Instant fromDateAsInstant(Clock clock) {
        return (fromDate != null) ? fromDate.atStartOfDay(clock.getZone()).toInstant() : null;
    }

    public Instant toDateAsInstant(Clock clock) {
        return (toDate != null) ? toDate.plusDays(1).atStartOfDay(clock.getZone()).toInstant() : null;
    }
}
