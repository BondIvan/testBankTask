package com.testtask.bankcardmanagement.model.dto.limit;

import com.testtask.bankcardmanagement.model.enums.LimitType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Card limit response object")
public record LimitResponse(
        @Schema(description = "Limit type", example = "DAILY, MONTHLY, NO_LIMIT")
        LimitType type,

        @Schema(description = "The maximum amount of funds that can be written off for this limit", example = "1000")
        BigDecimal maxAmount
) { }
