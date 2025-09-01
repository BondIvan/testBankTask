package com.testtask.bankcardmanagement.service.user;

import com.testtask.bankcardmanagement.exception.security.AccessDeniedException;
import com.testtask.bankcardmanagement.model.User;
import com.testtask.bankcardmanagement.model.dto.auth.AuthenticationResponse;
import com.testtask.bankcardmanagement.model.dto.auth.RegistrationRequest;
import com.testtask.bankcardmanagement.model.enums.UserRole;
import com.testtask.bankcardmanagement.service.security.SecurityService;
import com.testtask.bankcardmanagement.service.security.jwt.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminUserService {
    private final SecurityService securityService;
    private final AuthenticationService authenticationService;

    public AuthenticationResponse createUser(RegistrationRequest registrationRequest) {
        User currentUser = securityService.getCurrentUser();
        if(currentUser.getRole() != UserRole.ADMIN)
            throw new AccessDeniedException("Only admin can create a user");

        return authenticationService.register(registrationRequest);
    }

    public Boolean deleteUser(Long userId) {
        return null;
    }
}
