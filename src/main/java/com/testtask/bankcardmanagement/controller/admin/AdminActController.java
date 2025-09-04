package com.testtask.bankcardmanagement.controller.admin;

import com.testtask.bankcardmanagement.controller.PageableService;
import com.testtask.bankcardmanagement.model.dto.card.CardParamFilter;
import com.testtask.bankcardmanagement.model.dto.card.CardResponse;
import com.testtask.bankcardmanagement.model.dto.transaction.PaymentTransactionParamFilter;
import com.testtask.bankcardmanagement.model.dto.transaction.PaymentTransactionResponse;
import com.testtask.bankcardmanagement.service.user.AdminActService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/admin/")
public class AdminActController {
    private final AdminActService adminActService;
    private final PageableService pageableService;

    @GetMapping("/cards")
    public ResponseEntity<PagedModel<EntityModel<CardResponse>>> getAllCards(
            @Valid CardParamFilter filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") List<String> sortList,
            @RequestParam(defaultValue = "ASC") String sortOrder,
            PagedResourcesAssembler<CardResponse> assembler
    )
    {
        Pageable pageable = pageableService.createAdminCardPageable(page, size, sortList, sortOrder);
        Page<CardResponse> pageResponse = adminActService.getAllCards(filter, pageable);

        return ResponseEntity.ok(assembler.toModel(pageResponse));
    }

    @GetMapping("/transactions/{cardId}")
    public ResponseEntity<PagedModel<EntityModel<PaymentTransactionResponse>>> getTransactionsByCard(
            @PathVariable("cardId") Long cardId,
            @Valid PaymentTransactionParamFilter filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") List<String> sortList,
            @RequestParam(defaultValue = "ASC") String sortOrder,
            PagedResourcesAssembler<PaymentTransactionResponse> assembler
    ) {
        Pageable pageable = pageableService.createTransactionPageable(page, size, sortList, sortOrder);
        Page<PaymentTransactionResponse> pageResponse = adminActService.getAllTransactionsByCardId(cardId, filter, pageable);

        return ResponseEntity.ok(assembler.toModel(pageResponse));
    }

    @GetMapping("/transactions")
    public ResponseEntity<PagedModel<EntityModel<PaymentTransactionResponse>>> getTransactionsByAllCards(
            @Valid PaymentTransactionParamFilter filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") List<String> sortList,
            @RequestParam(defaultValue = "ASC") String sortOrder,
            PagedResourcesAssembler<PaymentTransactionResponse> assembler
    ) {
        Pageable pageable = pageableService.createTransactionPageable(page, size, sortList, sortOrder);
        Page<PaymentTransactionResponse> pageResponse = adminActService.getTransactionsByAllCards(filter, pageable);

        return ResponseEntity.ok(assembler.toModel(pageResponse));
    }
}
