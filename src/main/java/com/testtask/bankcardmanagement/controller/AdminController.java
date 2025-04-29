package com.testtask.bankcardmanagement.controller;

import com.testtask.bankcardmanagement.exception.other.InvalidSortFieldException;
import com.testtask.bankcardmanagement.model.dto.card.CardParamFilter;
import com.testtask.bankcardmanagement.model.dto.card.CardRequest;
import com.testtask.bankcardmanagement.model.dto.card.CardResponse;
import com.testtask.bankcardmanagement.model.dto.auth.AuthenticationResponse;
import com.testtask.bankcardmanagement.model.dto.auth.RegistrationRequest;
import com.testtask.bankcardmanagement.model.dto.limit.LimitUpdateRequest;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionParamFilter;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionResponse;
import com.testtask.bankcardmanagement.service.card.CardService;
import com.testtask.bankcardmanagement.service.security.jwt.AuthenticationService;
import com.testtask.bankcardmanagement.service.transaction.TransactionService;
import com.testtask.bankcardmanagement.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/admin/")
public class AdminController {
    private static final Set<String> SORTABLE_CARD_FIELDS = Set.of("id", "user.email", "status");
    private static final Set<String> SORTABLE_TRANSACTION_FIELDS = Set.of("id", "type", "amount");

    private final UserService userService;
    private final CardService cardService;
    private final AuthenticationService authenticationService;
    private final TransactionService transactionService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/create-user")
    public ResponseEntity<AuthenticationResponse> createUser(@RequestBody @Valid RegistrationRequest registrationRequest) {
        AuthenticationResponse authenticationResponse = authenticationService.register(registrationRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(authenticationResponse);
    }

//    @PreAuthorize("hasAuthority('ADMIN')")
//    @PutMapping("/update-user")
//    public ResponseEntity<UserResponse> updateUser(@RequestBody @Valid UserRequest userRequest, @RequestBody @Valid String email) {
//
//    }

//    @PreAuthorize("hasAuthority('ADMIN')")
//    @DeleteMapping("/delete-user")
//    public ResponseEntity<String> deleteUser(@RequestBody @Valid String email) {
//
//    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/create-card")
    public ResponseEntity<CardResponse> createCard(@RequestBody @Valid CardRequest cardRequest) {
        CardResponse response = cardService.createCard(cardRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/block-card/{cardId}")
    public ResponseEntity<CardResponse> blockingCard(@PathVariable("cardId") Long id) {
        CardResponse cardResponse = cardService.blockCard(id);
        return ResponseEntity.ok(cardResponse);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/activate-card/{cardId}")
    public ResponseEntity<CardResponse> activatingCard(@PathVariable("cardId") Long id) {
        CardResponse cardResponse = cardService.activateCard(id);
        return ResponseEntity.ok(cardResponse);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/delete-card/{cardId}")
    public ResponseEntity<String> deleteCard(@PathVariable("cardId") Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.ok("The card was successfully deleted");
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/get-all-cards")
    public ResponseEntity<Page<CardResponse>> getAllCards(
            @RequestBody() @Valid CardParamFilter paramFilter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") List<String> sortList,
            @RequestParam(defaultValue = "ASC") String sortOrder
    )
    {
        validateCardSortFields(sortList);
        return ResponseEntity.ok(
                cardService.getAllCards(paramFilter, page, size, sortList, sortOrder)
        );
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/get-transactions-by-card/{cardId}")
    public ResponseEntity<Page<TransactionResponse>> getTransactionsByCard(
            @PathVariable("cardId") Long cardId,
            @RequestBody @Valid TransactionParamFilter transactionParamFilter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") List<String> sortList,
            @RequestParam(defaultValue = "ASC") String sortOrder
    ) {
        validateTransactionSortFields(sortList);
        return ResponseEntity.ok(
                transactionService.getTransactionsByCard(cardId, transactionParamFilter, page, size, sortList, sortOrder)
        );
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/update-limits/{cardId}")
    public ResponseEntity<CardResponse> setDayCardLimit(@PathVariable("cardId") Long cardId,
                                                        @RequestBody @Valid LimitUpdateRequest limitUpdateRequest) {
        CardResponse cardResponse = cardService.updateCardLimit(cardId, limitUpdateRequest);
        return ResponseEntity.ok(cardResponse);
    }

    private void validateTransactionSortFields(List<String> sortList) {
        sortList.forEach(field -> {
            if(!SORTABLE_TRANSACTION_FIELDS.contains(field))
                throw new InvalidSortFieldException("Sorting by this field is not supported.");
        });
    }

    private void validateCardSortFields(List<String> sortList) {
        sortList.forEach(field -> {
            if(!SORTABLE_CARD_FIELDS.contains(field))
                throw new InvalidSortFieldException("Sorting by this field is not supported.");
        });
    }

}
