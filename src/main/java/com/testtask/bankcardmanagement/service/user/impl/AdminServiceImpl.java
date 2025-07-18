package com.testtask.bankcardmanagement.service.user.impl;

import com.testtask.bankcardmanagement.model.dto.auth.AuthenticationResponse;
import com.testtask.bankcardmanagement.model.dto.auth.RegistrationRequest;
import com.testtask.bankcardmanagement.service.security.jwt.AuthenticationService;
import com.testtask.bankcardmanagement.service.user.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AdminServiceImpl implements AdminService {
    private final AuthenticationService authenticationService;

    @Override
    public AuthenticationResponse createUser(RegistrationRequest registrationRequest) {
        return authenticationService.register(registrationRequest);
    }

    @Override
    public Boolean deleteUser(Long userId) {
        return null;
    }
}
