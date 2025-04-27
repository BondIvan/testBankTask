package com.testtask.bankcardmanagement.model.dto.card;

import com.testtask.bankcardmanagement.model.enums.CardStatus;

public record CardParamFilter(
        CardStatus status,
//        LocalDate expirationDate,
        String userEmail
) {}
