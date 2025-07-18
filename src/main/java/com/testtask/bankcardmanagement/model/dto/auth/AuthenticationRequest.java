package com.testtask.bankcardmanagement.model.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AuthenticationRequest(
        @Email(message = "Invalid email format")
        String email,

        @NotNull @NotBlank
        String password
) { }
