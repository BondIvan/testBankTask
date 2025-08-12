package com.testtask.bankcardmanagement.service.transaction;

import com.testtask.bankcardmanagement.model.Card;
import com.testtask.bankcardmanagement.model.Transaction;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionParamFilter;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionResponse;
import com.testtask.bankcardmanagement.model.enums.TransactionType;
import com.testtask.bankcardmanagement.model.mapper.TransactionMapper;
import com.testtask.bankcardmanagement.repository.TransactionRepository;
import com.testtask.bankcardmanagement.service.card.CardService;
import com.testtask.bankcardmanagement.service.security.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final SecurityService securityService;

    private Transaction createTransaction(Card card, TransactionType type, BigDecimal amount,
                                          String description, String target, LocalDateTime dateTime) {
        Transaction transaction = new Transaction();
        transaction.setCard(card);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setTargetMaskedCard(target);
        transaction.setTransactionDate(dateTime);

        return transaction;
    }

    public Page<TransactionResponse> getTransactionsByUserCard(Long cardId, TransactionParamFilter transactionParamFilter,
                                                               int page, int size,
                                                               List<String> sortList, String sortOrder) {
//        if(!cardService.validateCardOwnership(cardId))
//            throw new CardNotFoundException("Card does not belong to the user.");

        Long userId = securityService.getCurrentUser().getId();
        TransactionParamFilter updatedFilter = new TransactionParamFilter(
                cardId,
                transactionParamFilter.type(),
                transactionParamFilter.fromDate(),
                transactionParamFilter.toDate()
        );

        return getAllTransactionsByCard(updatedFilter, page, size, sortList, sortOrder, userId);
    }

    public Page<TransactionResponse> getTransactionsByCard(Long cardId, TransactionParamFilter transactionParamFilter,
                                                           int page, int size,
                                                           List<String> sortList, String sortOrder) {
//        if(!cardService.existById(cardId))
//            throw new CardNotFoundException("Card with such id not found.");


        TransactionParamFilter updatedFilter = new TransactionParamFilter(
                cardId,
                transactionParamFilter.type(),
                transactionParamFilter.fromDate(),
                transactionParamFilter.toDate()
        );

        return getAllTransactionsByCard(updatedFilter, page, size, sortList, sortOrder, null);
    }

    private Page<TransactionResponse> getAllTransactionsByCard(TransactionParamFilter filter,
                                                               int page, int size,
                                                               List<String> sortList, String sortOrder,
                                                               Long userId) {

        List<Sort.Order> sortOrderList = createSortOrder(sortList, sortOrder);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortOrderList));

        Specification<Transaction> spec = TransactionSpecification.build(filter, userId);

        List<TransactionResponse> transactions = transactionRepository.findAll(spec, pageable).stream()
                .map(transactionMapper::toTransactionResponse)
                .toList();

        return new PageImpl<>(
                transactions,
                pageable,
                transactions.size()
        );
    }

    private List<Sort.Order> createSortOrder(List<String> sortList, String sortOrder) {
        Sort.Direction sortDirection = Sort.Direction.fromString(sortOrder);
        return sortList.stream()
                .map(field -> new Sort.Order(sortDirection, field))
                .toList();
    }
}
