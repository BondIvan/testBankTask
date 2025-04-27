package com.testtask.bankcardmanagement.controller;

import com.testtask.bankcardmanagement.model.dto.transaction.TransactionResponse;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionTransferRequest;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionWriteOffRequest;
import com.testtask.bankcardmanagement.service.transaction.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/user/")
public class UserController {
    private final TransactionService transactionService;

//    @GetMapping("/get-all-cards")
//    public ResponseEntity<Page<CardResponse>> getAllUserCards() {
//
//    }

//    @PostMapping("/request-block-card")
//    public ResponseEntity<String> requestToBlockUserCard(@RequestBody @Valid BlockRequest blockRequest) {
//
//    }

//    @GetMapping("/get-transaction-by-user-card/{cardId}")
//    public ResponseEntity<Page<TransactionResponse>> getTransactionsByCard(@PathVariable("cardId") Long id) {
//
//    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/write-off")
    public ResponseEntity<TransactionResponse> writeOff(@RequestBody @Valid TransactionWriteOffRequest transactionWriteOffRequest) {
        TransactionResponse transactionResponse = transactionService.writeOff(transactionWriteOffRequest);
        return ResponseEntity.ok(transactionResponse);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(@RequestBody @Valid TransactionTransferRequest transactionTransferRequest) {
        TransactionResponse transactionResponse = transactionService.transfer(transactionTransferRequest);
        return ResponseEntity.ok(transactionResponse);
    }

}
