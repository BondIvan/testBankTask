package com.testtask.bankcardmanagement.controller.user;

import com.testtask.bankcardmanagement.model.dto.user.CommonUserResponse;
import com.testtask.bankcardmanagement.model.dto.user.EmailReplacementRequest;
import com.testtask.bankcardmanagement.model.dto.user.PasswordReplacementRequest;
import com.testtask.bankcardmanagement.service.user.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user/profile")
@RequiredArgsConstructor
public class UserProfileController {
    private final UserProfileService userProfileService;

    @PutMapping("/email")
    public ResponseEntity<CommonUserResponse> updateUserEmail(@RequestBody @Valid EmailReplacementRequest emailReplacementRequest) {
        return ResponseEntity.ok(userProfileService.changeUserEmail(emailReplacementRequest));
    }

    @PutMapping("/password") //TODO Add rate limiting for this endpoint
    public ResponseEntity<String> updatePassword(@RequestBody @Valid PasswordReplacementRequest passwordReplacementRequest) {
        userProfileService.changeUserPassword(passwordReplacementRequest);
        return ResponseEntity.ok("The password was successfully updated");
    }
}
