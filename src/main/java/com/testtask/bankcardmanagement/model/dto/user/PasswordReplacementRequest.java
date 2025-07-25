package com.testtask.bankcardmanagement.model.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PasswordReplacementRequest(
        @NotNull @NotBlank
        String oldPassword,

        @NotNull
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String newPassword //TODO @Password
) { }
