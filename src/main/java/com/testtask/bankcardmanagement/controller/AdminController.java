package com.testtask.bankcardmanagement.controller;

import com.testtask.bankcardmanagement.exception.InvalidSortFieldException;
import com.testtask.bankcardmanagement.model.dto.CardParamFilter;
import com.testtask.bankcardmanagement.model.dto.CardRequest;
import com.testtask.bankcardmanagement.model.dto.CardResponse;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionResponse;
import com.testtask.bankcardmanagement.model.dto.user.UserRequest;
import com.testtask.bankcardmanagement.model.dto.user.UserResponse;
import com.testtask.bankcardmanagement.service.card.CardService;
import com.testtask.bankcardmanagement.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/admin/")
public class AdminController {
    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "user.email", "status");

    private final UserService userService;
    private final CardService cardService;

    @GetMapping("/get-user")
    public ResponseEntity<UserResponse> getUserByEmail(@RequestBody @Valid String email) {
        UserResponse userResponse = userService.getUserByEmail(email);
        return ResponseEntity.ok(userResponse);
    }

    @PostMapping("/create-user")
    public ResponseEntity<UserResponse> createUser(@RequestBody @Valid UserRequest userRequest) {
        UserResponse userResponse = userService.createUser(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

//    @PutMapping("/update-user")
//    public ResponseEntity<UserResponse> updateUser(@RequestBody @Valid UserRequest userRequest, @RequestBody @Valid String email) {
//
//    }

//    @DeleteMapping("/delete-user")
//    public ResponseEntity<String> deleteUser(@RequestBody @Valid String email) {
//
//    }

    @PostMapping("/create-card")
    public ResponseEntity<CardResponse> createCard(@RequestBody @Valid CardRequest cardRequest) {
        CardResponse response = cardService.createCard(cardRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/block-card/{cardId}")
    public ResponseEntity<CardResponse> blockingCard(@PathVariable("cardId") Long id) {
        CardResponse cardResponse = cardService.blockCard(id);
        return ResponseEntity.ok(cardResponse);
    }

    @PutMapping("/activate-card/{cardId}")
    public ResponseEntity<CardResponse> activatingCard(@PathVariable("cardId") Long id) {
        CardResponse cardResponse = cardService.activateCard(id);
        return ResponseEntity.ok(cardResponse);
    }

    @DeleteMapping("/delete-card/{cardId}")
    public ResponseEntity<String> deleteCard(@PathVariable("cardId") Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.ok("The card was successfully deleted");
    }

    @GetMapping("/get-all-cards")
    public ResponseEntity<Page<CardResponse>> getAllCards(
            @RequestBody() @Valid CardParamFilter paramFilter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") List<String> sortList,
            @RequestParam(defaultValue = "ASC") String sortOrder
    )
    {
        validateSortFields(sortList);
        return ResponseEntity.ok(
                cardService.getAllCards(paramFilter, page, size, sortList, sortOrder)
        );
    }

//    @GetMapping("/get-transaction-by-card/{cardId}")
//    public ResponseEntity<Page<TransactionResponse>> getTransactionsByCard(@PathVariable("cardId") Long id) {
//
//    }

//    @PostMapping("/set-day-limit")
//    public ResponseEntity<?> setDayCardLimit() {
//
//    }

//    @PostMapping("/set-month-limit")
//    public ResponseEntity<?> setMonthCardLimit() {
//
//    }

    private void validateSortFields(List<String> sortList) {
        sortList.forEach(field -> {
            if(!SORTABLE_FIELDS.contains(field))
                throw new InvalidSortFieldException("Sorting by this field is not supported.");
        });
    }

}
