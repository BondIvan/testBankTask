package com.testtask.bankcardmanagement.service.security;

import com.testtask.bankcardmanagement.model.User;
import com.testtask.bankcardmanagement.service.security.jwt.JwtService;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SecurityService {
    private final JwtService jwtService;

    public SecurityService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated())
            throw new AuthenticationCredentialsNotFoundException("User not authenticated");

        return (User) authentication.getPrincipal();
    }

    public String updateSecurityContextWithNewCredentials(User updatedUser) {
        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                updatedUser,
                updatedUser.getPassword(),
                updatedUser.getAuthorities()
        );

        //TODO Нужно удалить/заблокировать старый jwt-токен

        SecurityContextHolder.getContext().setAuthentication(newAuth);

        return jwtService.generateToken(updatedUser);
    }
}
