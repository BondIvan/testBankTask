package com.testtask.bankcardmanagement.model.dto.limit;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

import java.util.List;

@Schema(description = "Card limit update request object")
public record LimitUpdateRequest(
        @Schema(description = "List of card limit request objects", example = "DAILY, MONTHLY, NO_LIMIT")
        @Valid
        List<LimitRequest> limits
) { }
