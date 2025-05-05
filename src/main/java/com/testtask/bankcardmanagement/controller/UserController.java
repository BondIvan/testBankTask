package com.testtask.bankcardmanagement.controller;

import com.testtask.bankcardmanagement.exception.other.InvalidSortFieldException;
import com.testtask.bankcardmanagement.model.dto.card.CardParamFilter;
import com.testtask.bankcardmanagement.model.dto.card.CardResponse;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionParamFilter;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionResponse;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionTransferRequest;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionWriteOffRequest;
import com.testtask.bankcardmanagement.model.dto.user.BlockRequest;
import com.testtask.bankcardmanagement.service.card.CardService;
import com.testtask.bankcardmanagement.service.transaction.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Tag(name = "User controller", description = "Endpoints for admins only")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/user/")
public class UserController {
    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "card.status", "amount");

    private final TransactionService transactionService;
    private final CardService cardService;

    @SecurityRequirement(name = "JWT")
    @Operation(
            summary = "Get all cards",
            description = "Allows you to get all user cards. Only an user can do this."
    )
    @GetMapping("/get-all-cards")
    public ResponseEntity<Page<CardResponse>> getAllUserCards(
            @RequestBody @Valid CardParamFilter cardParamFilter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    )
    {
        return ResponseEntity.ok(
                cardService.getAllCardsForCurrentUser(cardParamFilter, page, size)
        );
    }

    @PostMapping("/request-block-card")
    public ResponseEntity<String> requestToBlockUserCard(@RequestBody @Valid BlockRequest blockRequest) {
        //TODO Finish it off
        return null;
    }

    @SecurityRequirement(name = "JWT")
    @Operation(
            summary = "Get all card transactions",
            description = "Allows you to get all transactions on any user card by card Id. Only an user can do this."
    )
    @GetMapping("/get-transactions-by-user-card/{cardId}")
    public ResponseEntity<Page<TransactionResponse>> getTransactionsByCard(
            @PathVariable("cardId") Long cardId,
            @RequestBody @Valid @Parameter(description = "Can filter by this values") TransactionParamFilter transactionParamFilter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") List<String> sortList,
            @RequestParam(defaultValue = "ASC") String sortOrder
    ) {
        validateSortFields(sortList);
        return ResponseEntity.ok(
                transactionService.getTransactionsByUserCard(cardId, transactionParamFilter, page, size, sortList, sortOrder)
        );
    }

    @SecurityRequirement(name = "JWT")
    @Operation(
            summary = "Make a withdrawal",
            description = "Allows you to perform a debit operation using a card number. Only an user can do this."
    )
    @PostMapping("/write-off")
    public ResponseEntity<TransactionResponse> writeOff(@RequestBody @Valid TransactionWriteOffRequest transactionWriteOffRequest) {
        TransactionResponse transactionResponse = transactionService.writeOff(transactionWriteOffRequest);
        return ResponseEntity.ok(transactionResponse);
    }

    @SecurityRequirement(name = "JWT")
    @Operation(
            summary = "Transfer funds between cards",
            description = "Allows you to transfer funds between user cards using card numbers. Only an user can do this."
    )
    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(@RequestBody @Valid TransactionTransferRequest transactionTransferRequest) {
        TransactionResponse transactionResponse = transactionService.transfer(transactionTransferRequest);
        return ResponseEntity.ok(transactionResponse);
    }

    private void validateSortFields(List<String> sortList) {
        sortList.forEach(field -> {
            if(!SORTABLE_FIELDS.contains(field))
                throw new InvalidSortFieldException("Sorting by this field is not supported.");
        });
    }

}
