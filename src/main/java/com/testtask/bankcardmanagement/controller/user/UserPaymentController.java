package com.testtask.bankcardmanagement.controller.user;

import com.testtask.bankcardmanagement.model.dto.transaction.PaymentTransactionReplenishmentRequest;
import com.testtask.bankcardmanagement.model.dto.transaction.PaymentTransactionResponse;
import com.testtask.bankcardmanagement.model.dto.transaction.PaymentTransactionTransferRequest;
import com.testtask.bankcardmanagement.model.dto.transaction.PaymentTransactionWithdrawalRequest;
import com.testtask.bankcardmanagement.service.user.UserPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user/payment")
@RequiredArgsConstructor
public class UserPaymentController {
    private final UserPaymentService userPaymentService;

    @PostMapping("/withdrawal")
    public ResponseEntity<PaymentTransactionResponse> withdrawal(
            @RequestBody @Valid PaymentTransactionWithdrawalRequest request) {

        PaymentTransactionResponse response = userPaymentService.createWithdrawalTransaction(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/replenishment")
    public ResponseEntity<PaymentTransactionResponse> replenishment(
            @RequestBody @Valid PaymentTransactionReplenishmentRequest request) {

        PaymentTransactionResponse response = userPaymentService.createReplenishmentTransaction(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transfer")
    public ResponseEntity<PaymentTransactionResponse> transfer(
            @RequestBody @Valid PaymentTransactionTransferRequest request) {

        PaymentTransactionResponse response = userPaymentService.createTransferTransaction(request);
        return ResponseEntity.ok(response);
    }
}
