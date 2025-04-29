package com.testtask.bankcardmanagement.model.dto.card;

import com.testtask.bankcardmanagement.model.dto.limit.LimitRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Card creation request object")
public record CardRequest(
        @Schema(description = "Card number", example = "1234 1234 1234 1234")
        @NotNull
        @Pattern(regexp = "^\\d{16}$", message = "Invalid card number,should be - ____ ____ ____ ____")
        String cardNumber,

        @Schema(description = "The card is valid until that date")
        @Future(message = "The expiration date must be in future")
        LocalDate expirationDate,

        @Schema(description = "Owner mail", example = "example@mail.ru")
        @NotNull
        @Email(message = "Invalid email format")
        String ownerEmail,

        @Schema(description = "List of card limit request objects", example = "DAILY, MONTHLY, NO_LIMIT")
        @Valid
        List<LimitRequest> limits
) { }
