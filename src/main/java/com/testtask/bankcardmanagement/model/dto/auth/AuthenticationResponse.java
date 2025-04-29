package com.testtask.bankcardmanagement.model.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response object containing the token for authorization")
public record AuthenticationResponse(
        @Schema(description = "JWT authorization token",
                example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbjJAbWFpbC5ydSIsImlhdCI6MTc0NTc4MDQ1NiwiZXhwIj" +
                        "oxNzQ1Nzg3NjAwfQ.tT3aTDmzQ4m1TzbxRKLg8x2xQWvpP_ceoeMt8VPVcVQ"
        )
        String token
) { }
