package com.testtask.bankcardmanagement.service.security;

import com.testtask.bankcardmanagement.exception.security.AccessDeniedException;
import com.testtask.bankcardmanagement.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {
    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication.getPrincipal() instanceof User user) {
            return user;
        }

        throw new AccessDeniedException("User not authenticated");
    }
}
