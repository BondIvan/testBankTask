package com.testtask.bankcardmanagement.controller;

import com.testtask.bankcardmanagement.model.dto.card.CardParamFilter;
import com.testtask.bankcardmanagement.model.dto.card.CardResponse;
import com.testtask.bankcardmanagement.model.dto.limit.LimitResponse;
import com.testtask.bankcardmanagement.model.dto.limit.LimitUpdateRequest;
import com.testtask.bankcardmanagement.model.dto.transaction.PaymentTransactionParamFilter;
import com.testtask.bankcardmanagement.model.dto.transaction.PaymentTransactionReplenishmentRequest;
import com.testtask.bankcardmanagement.model.dto.transaction.PaymentTransactionResponse;
import com.testtask.bankcardmanagement.model.dto.transaction.PaymentTransactionTransferRequest;
import com.testtask.bankcardmanagement.model.dto.transaction.PaymentTransactionWithdrawalRequest;
import com.testtask.bankcardmanagement.model.dto.user.BlockRequest;
import com.testtask.bankcardmanagement.model.dto.user.CommonUserResponse;
import com.testtask.bankcardmanagement.model.dto.user.EmailReplacementRequest;
import com.testtask.bankcardmanagement.model.dto.user.PasswordReplacementRequest;
import com.testtask.bankcardmanagement.model.enums.LimitType;
import com.testtask.bankcardmanagement.service.user.UserActService;
import com.testtask.bankcardmanagement.service.user.UserPaymentService;
import com.testtask.bankcardmanagement.service.user.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/user/")
public class UserController {
    private final UserProfileService userProfileService;
    private final UserActService userActService;
    private final SortableFieldService sortableFieldService;
    private final UserPaymentService userPaymentService;

    @PostMapping("/update-email")
    public ResponseEntity<CommonUserResponse> updateUserEmail(
            @RequestBody @Valid EmailReplacementRequest emailReplacementRequest
    ) {
        return ResponseEntity.ok(userProfileService.changeUserEmail(emailReplacementRequest));
    }

    @PostMapping("/update-password") //TODO Add rate limiting for this endpoint
    public ResponseEntity<String> updatePassword(
            @RequestBody @Valid PasswordReplacementRequest passwordReplacementRequest
    ) {
        userProfileService.changeUserPassword(passwordReplacementRequest);
        return ResponseEntity.ok("The password was successfully updated");
    }

    @PostMapping("/card/{cardId}/update-limit")
    public ResponseEntity<List<LimitResponse>> updateCardLimit(
            @PathVariable("cardId") Long cardId,
            @RequestBody @Valid LimitUpdateRequest limitUpdateRequest) {

        return ResponseEntity.ok(userActService.updateCardLimit(cardId, limitUpdateRequest));
    }

    @DeleteMapping("/card/{cardId}/limit/{limitType}")
    public ResponseEntity<List<LimitResponse>> deleteCardLimit(
            @PathVariable("cardId") Long cardId,
            @PathVariable("limitType") LimitType limitType
    ) {
        return ResponseEntity.ok(userActService.deleteCardLimit(cardId, limitType));
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
        sortableFieldService.validCardFields(sortList);
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

    @GetMapping("/get-transactions/{cardId}")
    public ResponseEntity<PagedModel<EntityModel<PaymentTransactionResponse>>> getTransactionsByCard(
            @PathVariable("cardId") Long cardId,
            @Valid PaymentTransactionParamFilter filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") List<String> sortList,
            @RequestParam(defaultValue = "ASC") String sortOrder,
            PagedResourcesAssembler<PaymentTransactionResponse> assembler
    ) {
        sortableFieldService.validTransactionFields(sortList);
        List<Sort.Order> sorted = sortableFieldService.createSortOrder(sortList, sortOrder);

        Pageable pageable = PageRequest.of(page, size, Sort.by(sorted));
        Page<PaymentTransactionResponse> pageResponse = userActService.getAllTransactionsByCardId(cardId, filter, pageable);

        return ResponseEntity.ok(assembler.toModel(pageResponse));
    }

    @PostMapping("/payment/withdrawal")
    public ResponseEntity<PaymentTransactionResponse> withdrawal(
            @RequestBody @Valid PaymentTransactionWithdrawalRequest request) {

        PaymentTransactionResponse response = userPaymentService.createWithdrawalTransaction(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/payment/replenishment")
    public ResponseEntity<PaymentTransactionResponse> replenishment(
            @RequestBody @Valid PaymentTransactionReplenishmentRequest request) {

        PaymentTransactionResponse response = userPaymentService.createReplenishmentTransaction(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/payment/transfer")
    public ResponseEntity<PaymentTransactionResponse> transfer(
            @RequestBody @Valid PaymentTransactionTransferRequest request) {

        PaymentTransactionResponse response = userPaymentService.createTransferTransaction(request);
        return ResponseEntity.ok(response);
    }
}
