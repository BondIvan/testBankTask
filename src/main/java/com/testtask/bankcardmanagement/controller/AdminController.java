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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Tag(name = "Admin controller", description = "Endpoints for admins only")
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

    @SecurityRequirement(name = "JWT")
    @Operation(
            summary = "User registration",
            description = "Allows you to register a regular user or administrator. Only an administrator can do this."
    )
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

    @SecurityRequirement(name = "JWT")
    @Operation(
            summary = "Creating a card",
            description = "Allows you to create a card for a specific user using user email. Only an administrator can do this."
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/create-card")
    public ResponseEntity<CardResponse> createCard(@RequestBody @Valid CardRequest cardRequest) {
        CardResponse response = cardService.createCard(cardRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @SecurityRequirement(name = "JWT")
    @Operation(
            summary = "Block the card",
            description = "Allows you to block a user card by id. Only an administrator can do this."
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/block-card/{cardId}")
    public ResponseEntity<CardResponse> blockingCard(@PathVariable("cardId") Long id) {
        CardResponse cardResponse = cardService.blockCard(id);
        return ResponseEntity.ok(cardResponse);
    }

    @SecurityRequirement(name = "JWT")
    @Operation(
            summary = "Activate the card",
            description = "Allows you to activate a user card by id. Only an administrator can do this."
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/activate-card/{cardId}")
    public ResponseEntity<CardResponse> activatingCard(@PathVariable("cardId") Long id) {
        CardResponse cardResponse = cardService.activateCard(id);
        return ResponseEntity.ok(cardResponse);
    }

    @SecurityRequirement(name = "JWT")
    @Operation(
            summary = "Delete card",
            description = "Allows you to delete a user card by id. Only an administrator can do this."
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/delete-card/{cardId}")
    public ResponseEntity<String> deleteCard(@PathVariable("cardId") Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.ok("The card was successfully deleted");
    }

    @SecurityRequirement(name = "JWT")
    @Operation(
            summary = "Get all cards",
            description = "Allows you to get all the cards. Only an administrator can do this."
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/get-all-cards")
    public ResponseEntity<Page<CardResponse>> getAllCards(
            @RequestBody() @Valid @Parameter(description = "Can filter by this values") CardParamFilter paramFilter,
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

    @SecurityRequirement(name = "JWT")
    @Operation(
            summary = "Get all transactions of a specific card",
            description = "Allows you to get all transactions of any card by its id. Only an administrator can do this."
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/get-transactions-by-card/{cardId}")
    public ResponseEntity<Page<TransactionResponse>> getTransactionsByCard(
            @PathVariable("cardId") Long cardId,
            @RequestBody @Valid @Parameter(description = "Can filter by this values") TransactionParamFilter transactionParamFilter,
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

    @SecurityRequirement(name = "JWT")
    @Operation(
            summary = "Update card limits",
            description = "Allows you to update the set limits for any card by its ID. Only an administrator can do this."
    )
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
