package com.testtask.bankcardmanagement.model.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;

@Schema(description = "User authorization request object")
public record AuthenticationRequest(
        @Schema(description = "User mail", example = "example@mail.ru")
        @Email(message = "Invalid email format")
        String email,

        @Schema(description = "User password", example = "1234567890")
        String password
) { }
