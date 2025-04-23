package com.testtask.bankcardmanagement.model.dto;

import com.testtask.bankcardmanagement.model.dto.user.UserResponse;
import com.testtask.bankcardmanagement.model.enums.CardStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CardResponse(
    String maskedNumber,
    LocalDate expirationDate,
    UserResponse userResponse,
    CardStatus status,
    BigDecimal balance
) { }
