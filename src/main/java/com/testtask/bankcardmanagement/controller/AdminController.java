package com.testtask.bankcardmanagement.controller;

import com.testtask.bankcardmanagement.model.dto.auth.AuthenticationResponse;
import com.testtask.bankcardmanagement.model.dto.auth.RegistrationRequest;
import com.testtask.bankcardmanagement.model.dto.card.CardParamFilter;
import com.testtask.bankcardmanagement.model.dto.card.CardResponse;
import com.testtask.bankcardmanagement.model.dto.card.CreateCardRequest;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionResponse;
import com.testtask.bankcardmanagement.service.user.AdminActService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/admin/")
public class AdminController {
    private final AdminActService adminActService;
    private final ValidationSortableField sortableField;

    @PostMapping("/create-user")
    public ResponseEntity<AuthenticationResponse> createUser(@RequestBody @Valid RegistrationRequest registrationRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                adminActService.createUser(registrationRequest)
        );
    }

    @DeleteMapping("/delete-user")
    public ResponseEntity<String> deleteUser(@RequestBody @Valid String email) {
        //TODO Finish it off
        return null;
    }

    @PostMapping("/create-card")
    public ResponseEntity<CardResponse> createCard(@RequestBody @Valid CreateCardRequest createCardRequest) {
        CardResponse response = adminActService.createCard(createCardRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/block-card/{cardId}")
    public ResponseEntity<CardResponse> blockingCard(@PathVariable("cardId") Long id) {
//        CardResponse cardResponse = cardService.blockCard(id);
//        return ResponseEntity.ok(cardResponse);
        return null;
    }

    @PutMapping("/activate-card/{cardId}")
    public ResponseEntity<CardResponse> activatingCard(@PathVariable("cardId") Long id) {
//        CardResponse cardResponse = cardService.activateCard(id);
//        return ResponseEntity.ok(cardResponse);
        return null;
    }

    @DeleteMapping("/delete-card/{cardId}")
    public ResponseEntity<String> deleteCard(@PathVariable("cardId") Long cardId) {
        adminActService.deleteCard(cardId);
        return ResponseEntity.ok("The card was successfully deleted");
    }

    @GetMapping("/get-all-cards")
    public ResponseEntity<PagedModel<EntityModel<CardResponse>>> getAllCards(
            @Valid CardParamFilter filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") List<String> sortList,
            @RequestParam(defaultValue = "ASC") String sortOrder,
            PagedResourcesAssembler<CardResponse> assembler
    )
    {
        sortableField.validAdminCardFields(sortList);
        Page<CardResponse> cardPage = adminActService.getAllCards(filter, page, size, sortList, sortOrder);
        return ResponseEntity.ok(assembler.toModel(cardPage));
    }

    @GetMapping("/get-transactions-by-card/{cardId}")
    public ResponseEntity<Page<TransactionResponse>> getTransactionsByCard(
            @PathVariable("cardId") Long cardId,
//            @RequestBody @Valid TransactionParamFilter transactionParamFilter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") List<String> sortList,
            @RequestParam(defaultValue = "ASC") String sortOrder
    ) {
        sortableField.validTransactionFields(sortList);
        return ResponseEntity.ok(
                null
//                transactionService.getTransactionsByCard(cardId, transactionParamFilter, type, from, to, page, size, sortList, sortOrder)
        );
    }
}
