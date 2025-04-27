package com.testtask.bankcardmanagement.model.dto.auth;

public record RegistrationRequest(
    String email,
    String password
) { }
