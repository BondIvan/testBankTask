package com.testtask.bankcardmanagement.model.dto.card;

import com.testtask.bankcardmanagement.model.dto.limit.LimitResponse;
import com.testtask.bankcardmanagement.model.dto.user.CommonUserResponse;
import com.testtask.bankcardmanagement.model.enums.CardStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CardResponse(
        String maskedNumber,

        LocalDate expirationDate,

        CommonUserResponse owner,

        CardStatus status,

        BigDecimal balance,

        List<LimitResponse> limitResponseList
) { }
