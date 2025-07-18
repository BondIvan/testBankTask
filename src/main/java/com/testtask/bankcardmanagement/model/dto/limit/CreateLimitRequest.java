package com.testtask.bankcardmanagement.model.dto.limit;

import com.testtask.bankcardmanagement.exception.limit.LimitException;
import com.testtask.bankcardmanagement.model.enums.LimitType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record LimitRequest(
        @NotNull
        LimitType type,

        @Positive
        @DecimalMin(value = "1.00", message = "Min value - 1.00")
        BigDecimal maxAmount
) {
        public LimitRequest {
                if(type == LimitType.NO_LIMIT && maxAmount != null)
                        throw new LimitException("NO_LIMIT cannot have maxAmount");
        }
}
