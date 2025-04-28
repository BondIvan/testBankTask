package com.testtask.bankcardmanagement.model.dto.auth;

import jakarta.validation.constraints.Email;

public record AuthenticationRequest(
        @Email(message = "Invalid email format")
        String email,

        String password
) { }
