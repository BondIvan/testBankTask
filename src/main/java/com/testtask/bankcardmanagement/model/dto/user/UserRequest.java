package com.testtask.bankcardmanagement.model.dto.user;

import com.testtask.bankcardmanagement.model.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Create user request object")
public record UserRequest(
        @Schema(description = "User mail", example = "example@mail.ru")
        @Email(message = "Invalid email format")
        String email,

        @Schema(description = "User password", example = "1234567890")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password,

        @Schema(description = "User role", example = "USER, ADMIN")
        @NotNull
        UserRole role
) { }
