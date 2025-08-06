package com.testtask.bankcardmanagement.controller;

import com.testtask.bankcardmanagement.model.dto.card.CardParamFilter;
import com.testtask.bankcardmanagement.model.dto.card.CardResponse;
import com.testtask.bankcardmanagement.model.dto.limit.LimitUpdateRequest;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionResponse;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionTransferRequest;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionWriteOffRequest;
import com.testtask.bankcardmanagement.model.dto.user.BlockRequest;
import com.testtask.bankcardmanagement.model.dto.user.CommonUserResponse;
import com.testtask.bankcardmanagement.model.dto.user.EmailReplacementRequest;
import com.testtask.bankcardmanagement.model.dto.user.PasswordReplacementRequest;
import com.testtask.bankcardmanagement.model.enums.TransactionType;
import com.testtask.bankcardmanagement.service.user.UserActService;
import com.testtask.bankcardmanagement.service.user.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/user/")
public class UserController {
    private final UserProfileService userProfileService;
    private final UserActService userActService;
    private final ValidationSortableField sortableField;

    @PostMapping("/update-email")
    public ResponseEntity<CommonUserResponse> updateUserEmail(
            @RequestBody @Valid EmailReplacementRequest emailReplacementRequest
    ) {
        return ResponseEntity.ok(userProfileService.changeUserEmail(emailReplacementRequest));
    }

    @PostMapping("/update-password") //TODO Добавить rate limiting на этот эндпоинт
    public ResponseEntity<String> updatePassword(
            @RequestBody @Valid PasswordReplacementRequest passwordReplacementRequest
    ) {
        userProfileService.changeUserPassword(passwordReplacementRequest);
        return ResponseEntity.ok("The password was successfully updated");
    }

    @PostMapping("/card/{cardId}/update-limit")
    public ResponseEntity<CardResponse> updateCardLimit(
            @PathVariable("cardId") Long cardId,
            @RequestBody @Valid LimitUpdateRequest limitUpdateRequest) {

        return ResponseEntity.ok(userActService.updateCardLimit(cardId, limitUpdateRequest));
    }

    @GetMapping("/get-all-cards")
    public ResponseEntity<PagedModel<EntityModel<CardResponse>>> getAllUserCards(
            @Valid CardParamFilter filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") List<String> sortList,
            @RequestParam(defaultValue = "ASC") String sortOrder,
            PagedResourcesAssembler<CardResponse> assembler
    )
    {
        sortableField.validCardFields(sortList);
        Page<CardResponse> cardPage = userActService.getAllCards(filter, page, size, sortList, sortOrder);
        return ResponseEntity.ok(assembler.toModel(cardPage));
    }

    @PostMapping("/request-block-card")
    public ResponseEntity<String> requestToBlockUserCard(@RequestBody @Valid BlockRequest blockRequest) {
        //TODO Finish it off
        return null;
    }

//    @PostMapping("/request-active-card")
//    public ResponseEntity<String> requestToActivateUserCard(@RequestBody @Valid ActivateRequest activateRequest) {
//        //TODO Finish if off
//        return null;
//    }

    @GetMapping("/get-transactions-by-user-card/{cardId}")
    public ResponseEntity<Page<TransactionResponse>> getTransactionsByCard(
            @PathVariable("cardId") Long cardId,
//            @RequestBody @Valid TransactionParamFilter transactionParamFilter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "null") TransactionType type,
            @RequestParam(defaultValue = "null") LocalDateTime from,
            @RequestParam(defaultValue = "null") LocalDateTime to,
            @RequestParam(defaultValue = "id") List<String> sortList,
            @RequestParam(defaultValue = "ASC") String sortOrder
    ) {
        sortableField.validTransactionFields(sortList);
        return ResponseEntity.ok(
                null
//                transactionService.getTransactionsByUserCard(cardId, transactionParamFilter, page, size,
//                        type, from, to, sortList, sortOrder)
        );
    }

    @PostMapping("/write-off")
    public ResponseEntity<TransactionResponse> writeOff(@RequestBody @Valid TransactionWriteOffRequest transactionWriteOffRequest) {
//        TransactionResponse transactionResponse = transactionService.writeOff(transactionWriteOffRequest);
//        return ResponseEntity.ok(transactionResponse);
        return null;
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(@RequestBody @Valid TransactionTransferRequest transactionTransferRequest) {
//        TransactionResponse transactionResponse = transactionService.transfer(transactionTransferRequest);
//        return ResponseEntity.ok(transactionResponse);
        return null;
    }
}
