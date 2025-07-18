package com.testtask.bankcardmanagement.model.dto.user;

import jakarta.validation.constraints.Email;

public record EmailReplacementRequest(
        @Email(message = "Invalid email format")
        String newEmail
) { }
