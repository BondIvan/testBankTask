package com.testtask.bankcardmanagement.model.dto;

import com.testtask.bankcardmanagement.model.User;
import com.testtask.bankcardmanagement.model.enums.CardStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CardResponse(
    String maskedNumber,
    LocalDate expirationDate,
    User user,
    CardStatus status,
    BigDecimal balance
) { }
