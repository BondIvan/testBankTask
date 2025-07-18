package com.testtask.bankcardmanagement.service.user;

import com.testtask.bankcardmanagement.model.dto.auth.AuthenticationResponse;
import com.testtask.bankcardmanagement.model.dto.auth.RegistrationRequest;

public interface AdminService {
    AuthenticationResponse createUser(RegistrationRequest registrationRequest);
    Boolean deleteUser(Long userId);
}
