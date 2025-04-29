package com.testtask.bankcardmanagement.model.dto.auth;

import com.testtask.bankcardmanagement.model.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

@Schema(description = "User registration request object")
public record RegistrationRequest(
        @Schema(description = "User mail", example = "example@mail.ru")
        @Email(message = "Invalid email format")
        String email,

        @Schema(description = "User password", example = "1234567890")
        String password,

        @Schema(description = "User role", example = "ADMIN or USER")
        @NotNull
        UserRole role
) { }
