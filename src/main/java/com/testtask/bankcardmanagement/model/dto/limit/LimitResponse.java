package com.testtask.bankcardmanagement.model.dto.limit;

import com.testtask.bankcardmanagement.model.enums.LimitType;

import java.math.BigDecimal;

public record LimitResponse(
    LimitType type,
    BigDecimal maxAmount
) { }
