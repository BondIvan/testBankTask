package com.testtask.bankcardmanagement.model.dto.auth;

public record AuthenticationRequest(
    String email,
    String password
) { }
