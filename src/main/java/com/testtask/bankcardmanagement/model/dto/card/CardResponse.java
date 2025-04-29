package com.testtask.bankcardmanagement.model.dto.card;

import com.testtask.bankcardmanagement.model.dto.limit.LimitResponse;
import com.testtask.bankcardmanagement.model.dto.user.UserResponse;
import com.testtask.bankcardmanagement.model.enums.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "Card response object")
public record CardResponse(
        @Schema(description = "Masked card number", example = "**** **** **** 1234")
        String maskedNumber,

        @Schema(description = "Date until which the card is valid")
        LocalDate expirationDate,

        @Schema(description = "Card user response object")
        UserResponse userResponse,

        @Schema(description = "Card status", example = "ACTIVE, BLOCKED, EXPIRED")
        CardStatus status,

        @Schema(description = "Current balance of the card", example = "123,45")
        BigDecimal balance,

        @Schema(description = "List of limit response objects for the card")
        List<LimitResponse> limitResponseList
) { }
