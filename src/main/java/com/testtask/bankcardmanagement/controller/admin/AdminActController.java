package com.testtask.bankcardmanagement.controller.admin;

import com.testtask.bankcardmanagement.controller.SortableFieldService;
import com.testtask.bankcardmanagement.model.dto.transaction.PaymentTransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    private final SortableFieldService sortableFieldService;

    @GetMapping("/get-transactions-by-card/{cardId}")
    public ResponseEntity<Page<PaymentTransactionResponse>> getTransactionsByCard(
            @PathVariable("cardId") Long cardId,
//            @RequestBody @Valid TransactionParamFilter transactionParamFilter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") List<String> sortList,
            @RequestParam(defaultValue = "ASC") String sortOrder
    ) {
        sortableFieldService.validTransactionFields(sortList);
        return ResponseEntity.ok(
                null
//                transactionService.getTransactionsByCard(cardId, transactionParamFilter, type, from, to, page, size, sortList, sortOrder)
        );
    }
}
