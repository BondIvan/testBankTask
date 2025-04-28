package com.testtask.bankcardmanagement.model.dto.user;

import com.testtask.bankcardmanagement.model.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @Email(message = "Invalid email format")
        String email,

        @Size(min = 8)
        String password,

        @NotNull
        UserRole role
) { }
