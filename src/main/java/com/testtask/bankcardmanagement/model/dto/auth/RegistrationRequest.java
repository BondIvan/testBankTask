package com.testtask.bankcardmanagement.model.dto.auth;

import com.testtask.bankcardmanagement.model.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistrationRequest(
        @Email(message = "Invalid email format")
        String email,

        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password,

        @NotNull
        UserRole role
) { }
