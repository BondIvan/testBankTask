package com.testtask.bankcardmanagement.model.dto.limit;

import jakarta.validation.Valid;

import java.util.List;

public record LimitUpdateRequest(
        @Valid
        List<LimitRequest> limits
) { }
