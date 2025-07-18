package com.testtask.bankcardmanagement.service.user.impl;

import com.testtask.bankcardmanagement.exception.security.AccessDeniedException;
import com.testtask.bankcardmanagement.exception.user.BadCredentialsException;
import com.testtask.bankcardmanagement.model.User;
import com.testtask.bankcardmanagement.model.dto.user.CommonUserResponse;
import com.testtask.bankcardmanagement.model.dto.user.EmailReplacementRequest;
import com.testtask.bankcardmanagement.model.dto.user.PasswordReplacementRequest;
import com.testtask.bankcardmanagement.model.mapper.UserMapper;
import com.testtask.bankcardmanagement.repository.UserRepository;
import com.testtask.bankcardmanagement.service.security.SecurityUtil;
import com.testtask.bankcardmanagement.service.user.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Service
public class UserProfileServiceImpl implements UserProfileService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public CommonUserResponse changeUserEmail(Long userId, EmailReplacementRequest emailReplacementRequest) {
        return userMapper.toUserResponse(
                userRepository.changeUserEmailByUserId(userId, emailReplacementRequest.newEmail())
        );
    }

    @Override
    public Boolean changeUserPassword(Long userId, PasswordReplacementRequest passwordReplacementRequest) {
        User currentUser = SecurityUtil.getCurrentUser();

        if(currentUser.getId().equals(userId))
            throw new AccessDeniedException("Cannot change another user's password");

        if(passwordEncoder.matches(passwordReplacementRequest.oldPassword(), currentUser.getPassword()))
            throw new BadCredentialsException("Incorrect current password");

        String encodedNewPassword = passwordEncoder.encode(passwordReplacementRequest.newPassword());
        userRepository.changeUserPasswordByUserId(userId, encodedNewPassword);

        return true;
    }
}
