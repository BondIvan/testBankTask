package com.testtask.bankcardmanagement.service.user;

import com.testtask.bankcardmanagement.model.dto.user.CommonUserResponse;
import com.testtask.bankcardmanagement.model.dto.user.EmailReplacementRequest;
import com.testtask.bankcardmanagement.model.dto.user.PasswordReplacementRequest;

public interface UserProfileService {
    CommonUserResponse changeUserEmail(Long userId, EmailReplacementRequest emailReplacementRequest);
    Boolean changeUserPassword(Long userId, PasswordReplacementRequest passwordReplacementRequest);
}
