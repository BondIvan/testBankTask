package com.testtask.bankcardmanagement.model.dto.auth;

import com.testtask.bankcardmanagement.model.enums.UserRole;

public record RegistrationRequest(
    String email,
    String password,
    UserRole role
) { }
