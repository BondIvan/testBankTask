package com.testtask.bankcardmanagement.service.transaction;

import com.testtask.bankcardmanagement.model.dto.transaction.TransactionParamFilter;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionResponse;
import com.testtask.bankcardmanagement.model.mapper.TransactionMapper;
import com.testtask.bankcardmanagement.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class TransactionService {
    private final PaymentTransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    public Page<TransactionResponse> getAllTransactionsByCardId(Long cardId, TransactionParamFilter filter, int page, int size,
                                                       List<String> sortList, String sortOrder) {

        List<Sort.Order> sortOrderList = createSortOrder(sortList, sortOrder);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortOrderList));
//        Specification<Transaction> transactionSpec = TransactionSpecification.build(filter, cardId);

        return null;
    }

    private List<Sort.Order> createSortOrder(List<String> sortList, String sortOrder) {
        Sort.Direction sortDirection = Sort.Direction.fromString(sortOrder);
        return sortList.stream()
                .map(field -> new Sort.Order(sortDirection, field))
                .toList();
    }
}
