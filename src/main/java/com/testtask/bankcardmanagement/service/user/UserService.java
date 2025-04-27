package com.testtask.bankcardmanagement.service.user;

import com.testtask.bankcardmanagement.model.dto.user.UserRequest;
import com.testtask.bankcardmanagement.model.dto.user.UserResponse;

import java.util.List;

public interface UserService {
    List<UserResponse> getAllUsers();
    UserResponse updateUser(UserRequest userRequest);
    void deleteUser(Long id);
}
