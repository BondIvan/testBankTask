package com.testtask.bankcardmanagement.model.dto.card;

import com.testtask.bankcardmanagement.model.enums.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;

@Schema(description = "An object containing fields by which you can filter cards")
public record CardParamFilter(
        @Schema(description = "Card status", example = "ACTIVE, BLOCKED, EXPIRED")
        CardStatus status,

//        LocalDate expirationDate,

        @Schema(description = "User mail", example = "example@mail.ru")
        @Email
        String userEmail
) {}
