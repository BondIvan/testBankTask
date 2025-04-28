package com.testtask.bankcardmanagement.model.dto.auth;

import com.testtask.bankcardmanagement.model.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record RegistrationRequest(
        @Email(message = "Invalid email format")
        String email,

        String password,

        @NotNull
        UserRole role
) { }
