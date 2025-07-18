package com.testtask.bankcardmanagement.model.mapper;

import com.testtask.bankcardmanagement.model.User;
import com.testtask.bankcardmanagement.model.dto.user.CommonUserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public CommonUserResponse toUserResponse(User user) {
        return new CommonUserResponse(
                user.getEmail(),
                user.getRole().name()
        );
    }

}
