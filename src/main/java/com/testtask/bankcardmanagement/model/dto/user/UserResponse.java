package com.testtask.bankcardmanagement.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User response object")
public record UserResponse(
        @Schema(description = "User mail", example = "example@mail.ru")
        String email,

        @Schema(description = "User role", example = "USER, ADMIN")
        String role
) { }
