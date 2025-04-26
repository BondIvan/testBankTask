package com.testtask.bankcardmanagement.controller;

import com.testtask.bankcardmanagement.model.dto.transaction.TransactionResponse;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionTransferRequest;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionWriteOffRequest;
import com.testtask.bankcardmanagement.service.transaction.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users/transaction")
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping("/write-off")
    public ResponseEntity<TransactionResponse> writeOff(@RequestBody @Valid TransactionWriteOffRequest transactionWriteOffRequest) {
        TransactionResponse transactionResponse = transactionService.writeOff(transactionWriteOffRequest);
        return ResponseEntity.ok(transactionResponse);
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(@RequestBody @Valid TransactionTransferRequest transactionTransferRequest) {
        TransactionResponse transactionResponse = transactionService.transfer(transactionTransferRequest);
        return ResponseEntity.ok(transactionResponse);
    }

}
