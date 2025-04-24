package com.testtask.bankcardmanagement.service.user.impl;

import com.testtask.bankcardmanagement.exception.SomeDBException;
import com.testtask.bankcardmanagement.exception.UserDuplicateException;
import com.testtask.bankcardmanagement.exception.UserNotFoundException;
import com.testtask.bankcardmanagement.model.User;
import com.testtask.bankcardmanagement.model.dto.user.UserRequest;
import com.testtask.bankcardmanagement.model.dto.user.UserResponse;
import com.testtask.bankcardmanagement.model.mapper.UserMapper;
import com.testtask.bankcardmanagement.repository.UserRepository;
import com.testtask.bankcardmanagement.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public UserResponse createUser(UserRequest userRequest) {
        if(userRepository.existsByEmail(userRequest.email()))
            throw new UserDuplicateException("A user with this email already exists.");

        User user = new User();
        user.setEmail(userRequest.email());
        user.setPassword(passwordEncoder.encode(userRequest.password()));
        user.setRole(userRequest.role());

        try {
            User savedUser = userRepository.save(user);
            return userMapper.toUserResponse(savedUser);
        } catch (DataIntegrityViolationException e) {
            throw new SomeDBException("A user with this email already exists in the database. " + e.getMessage(), e);
        }
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        Optional<User> optionalUser = userRepository.findUserByEmail(email);
        if(optionalUser.isEmpty())
            throw new UserNotFoundException("User with such email not found");

        User user = optionalUser.get();
        return userMapper.toUserResponse(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return List.of();
    }

    @Override
    public UserResponse updateUser(UserRequest userRequest) {
        return null;
    }

    @Override
    public void deleteUser(UUID id) {

    }
}
