package com.testtask.bankcardmanagement.model.dto.card;

import com.testtask.bankcardmanagement.model.dto.limit.LimitResponse;
import com.testtask.bankcardmanagement.model.dto.user.UserResponse;
import com.testtask.bankcardmanagement.model.enums.CardStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CardResponse(
    String maskedNumber,
    LocalDate expirationDate,
    UserResponse userResponse,
    CardStatus status,
    BigDecimal balance,
    List<LimitResponse> limitResponseList
) { }
