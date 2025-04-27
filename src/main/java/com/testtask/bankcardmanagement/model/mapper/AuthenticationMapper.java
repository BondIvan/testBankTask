package com.testtask.bankcardmanagement.model.mapper;

import com.testtask.bankcardmanagement.model.dto.auth.AuthenticationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationMapper {

    public AuthenticationResponse toAuthenticationResponse(String token) {
        return new AuthenticationResponse(token);
    }

}
