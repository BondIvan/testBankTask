package com.testtask.bankcardmanagement.service.user;

import com.testtask.bankcardmanagement.model.dto.user.CommonUserResponse;
import com.testtask.bankcardmanagement.model.dto.user.EmailReplacementRequest;
import com.testtask.bankcardmanagement.model.dto.user.PasswordReplacementRequest;

public interface UserProfileService {
    CommonUserResponse changeUserEmail(EmailReplacementRequest emailReplacementRequest);
    Boolean changeUserPassword(PasswordReplacementRequest passwordReplacementRequest);
}
