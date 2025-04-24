package com.testtask.bankcardmanagement.model.dto;

import com.testtask.bankcardmanagement.model.enums.CardStatus;

import java.time.LocalDate;

public record CardParamFilter(
        CardStatus status,
//        LocalDate expirationDate,
        String userEmail
) {}
