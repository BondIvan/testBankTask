package com.testtask.bankcardmanagement.model.mapper;

import com.testtask.bankcardmanagement.model.User;
import com.testtask.bankcardmanagement.model.dto.user.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getEmail(),
                user.getRole().name()
        );
    }

}
