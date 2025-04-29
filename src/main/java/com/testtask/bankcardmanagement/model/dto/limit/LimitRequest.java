package com.testtask.bankcardmanagement.model.dto.limit;

import com.testtask.bankcardmanagement.exception.limit.LimitException;
import com.testtask.bankcardmanagement.model.enums.LimitType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Card limit request object")
public record LimitRequest(
        @Schema(description = "Limit type", example = "DAILY, MONTHLY, NO_LIMIT")
        @NotNull
        LimitType type,

        @Schema(description = "The maximum amount of funds that can be written off for this limit", example = "1000")
        @Positive
        @DecimalMin(value = "1.00", message = "Min value - 1.00")
        BigDecimal maxAmount
) {
        public LimitRequest {
                if(type == LimitType.NO_LIMIT && maxAmount != null)
                        throw new LimitException("NO_LIMIT cannot have maxAmount");
        }
}
