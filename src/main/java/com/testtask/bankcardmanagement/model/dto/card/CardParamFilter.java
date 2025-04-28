package com.testtask.bankcardmanagement.model.dto.card;

import com.testtask.bankcardmanagement.model.enums.CardStatus;
import jakarta.validation.constraints.Email;

public record CardParamFilter(
        CardStatus status,
//        LocalDate expirationDate,
        @Email
        String userEmail
) {}
