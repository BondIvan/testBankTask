package com.testtask.bankcardmanagement.service.user.impl;

import com.testtask.bankcardmanagement.model.dto.user.UserRequest;
import com.testtask.bankcardmanagement.model.dto.user.UserResponse;
import com.testtask.bankcardmanagement.model.mapper.UserMapper;
import com.testtask.bankcardmanagement.repository.UserRepository;
import com.testtask.bankcardmanagement.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserResponse> getAllUsers() {
        return List.of();
    }

    @Override
    public UserResponse updateUser(UserRequest userRequest) {
        return null;
    }

    @Override
    public void deleteUser(Long id) {

    }
}
