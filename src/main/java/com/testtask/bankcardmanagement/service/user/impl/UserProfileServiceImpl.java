package com.testtask.bankcardmanagement.service.user.impl;

import com.testtask.bankcardmanagement.exception.user.BadCredentialsException;
import com.testtask.bankcardmanagement.exception.user.UserNotFoundException;
import com.testtask.bankcardmanagement.model.User;
import com.testtask.bankcardmanagement.model.dto.user.CommonUserResponse;
import com.testtask.bankcardmanagement.model.dto.user.EmailReplacementRequest;
import com.testtask.bankcardmanagement.model.dto.user.PasswordReplacementRequest;
import com.testtask.bankcardmanagement.model.mapper.UserMapper;
import com.testtask.bankcardmanagement.repository.UserRepository;
import com.testtask.bankcardmanagement.service.security.SecurityService;
import com.testtask.bankcardmanagement.service.user.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Service
public class UserProfileServiceImpl implements UserProfileService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final SecurityService securityService;

    @Override
    public CommonUserResponse changeUserEmail(EmailReplacementRequest emailReplacementRequest) {
        Long currentUserId = securityService.getCurrentUser().getId();

        User userFromDB = userRepository.findUserById(currentUserId)
                        .orElseThrow(() -> new UserNotFoundException("User with such id not found"));

        userFromDB.setEmail(emailReplacementRequest.newEmail());

//        userRepository.save(user); Will automatically because of @Transactional

        securityService.updateSecurityContextWithNewCredentials(userFromDB);

        return userMapper.toUserResponse(userFromDB); //TODO Return new authUserDTO with new jwt-token
    }

    @Override
    public Boolean changeUserPassword(PasswordReplacementRequest passwordReplacementRequest) {
        User currentUser = securityService.getCurrentUser();

        if(!passwordEncoder.matches(passwordReplacementRequest.oldPassword(), currentUser.getPassword()))
            throw new BadCredentialsException("Incorrect current password");

        User userFromDB = userRepository.findUserById(currentUser.getId())
                .orElseThrow(() -> new UserNotFoundException("User with such id not found"));

        String encodedNewPassword = passwordEncoder.encode(passwordReplacementRequest.newPassword());
        userFromDB.setPassword(encodedNewPassword);

//        userRepository.save(currentUser); Will automatically because of @Transactional

        securityService.updateSecurityContextWithNewCredentials(userFromDB); //TODO Return new authUserDTO with new jwt-token

        return true;
    }
}
