package com.testtask.bankcardmanagement.controller;

import com.testtask.bankcardmanagement.model.User;
import com.testtask.bankcardmanagement.model.dto.CardParamFilter;
import com.testtask.bankcardmanagement.model.dto.CardResponse;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionResponse;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionTransferRequest;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionWriteOffRequest;
import com.testtask.bankcardmanagement.service.card.CardService;
import com.testtask.bankcardmanagement.service.transaction.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/user/")
public class UserController {
    private final TransactionService transactionService;
    private final CardService cardService;

    @PreAuthorize("hasAuthority('USER')")
    @GetMapping("/get-all-cards")
    public ResponseEntity<Page<CardResponse>> getAllUserCards(
            @RequestBody @Valid CardParamFilter cardParamFilter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    )
    {
        System.out.println("Current user: " + (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        return ResponseEntity.ok(
                cardService.getAllCardsForCurrentUser(cardParamFilter, page, size)
        );
    }

//    @PreAuthorize("hasAuthority('USER')")
//    @PostMapping("/request-block-card")
//    public ResponseEntity<String> requestToBlockUserCard(@RequestBody @Valid BlockRequest blockRequest) {
//
//    }

//    @PreAuthorize("hasAuthority('USER')")
//    @GetMapping("/get-transactions-by-user-card/{cardId}")
//    public ResponseEntity<Page<TransactionResponse>> getTransactionsByCard(@PathVariable("cardId") Long id) {
//
//    }

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/write-off")
    public ResponseEntity<TransactionResponse> writeOff(@RequestBody @Valid TransactionWriteOffRequest transactionWriteOffRequest) {
        TransactionResponse transactionResponse = transactionService.writeOff(transactionWriteOffRequest);
        return ResponseEntity.ok(transactionResponse);
    }

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(@RequestBody @Valid TransactionTransferRequest transactionTransferRequest) {
        TransactionResponse transactionResponse = transactionService.transfer(transactionTransferRequest);
        return ResponseEntity.ok(transactionResponse);
    }

}
