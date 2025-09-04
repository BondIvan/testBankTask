package com.testtask.bankcardmanagement.service.transaction;

import com.testtask.bankcardmanagement.model.dto.transaction.PaymentTransactionParamFilter;
import com.testtask.bankcardmanagement.model.transaction.AbstractPaymentTransaction;
import com.testtask.bankcardmanagement.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Clock;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final PaymentTransactionRepository transactionRepository;
    private final Clock clock;

    public Page<AbstractPaymentTransaction> getAllTransactionsByCardId(Long cardId, PaymentTransactionParamFilter filter,
                                                                       Pageable pageable) {

        Specification<AbstractPaymentTransaction> spec = PaymentTransactionSpecification
                .byCardId(cardId)
                .and(PaymentTransactionSpecification.byUserId(filter.userId()))
                .and(PaymentTransactionSpecification.byType(filter.type()))
                .and(PaymentTransactionSpecification.byCreatedAt(filter.fromDateAsInstant(clock), filter.toDateAsInstant(clock)))
                .and(PaymentTransactionSpecification.byAmount(filter.fromAmount(), filter.toAmount()));

        return transactionRepository.findAll(spec, pageable);
    }

    public Page<AbstractPaymentTransaction> getTransactionsByAllCards(PaymentTransactionParamFilter filter,
                                                                       Pageable pageable) {

        Specification<AbstractPaymentTransaction> spec = PaymentTransactionSpecification
                .byUserId(filter.userId())
                .and(PaymentTransactionSpecification.byType(filter.type()))
                .and(PaymentTransactionSpecification.byCreatedAt(filter.fromDateAsInstant(clock), filter.toDateAsInstant(clock)))
                .and(PaymentTransactionSpecification.byAmount(filter.fromAmount(), filter.toAmount()));

        return transactionRepository.findAll(spec, pageable);
    }

    public Page<AbstractPaymentTransaction> getTransactionsByAllCardsByAdmin(PaymentTransactionParamFilter filter,
                                                                             Pageable pageable) {

        Specification<AbstractPaymentTransaction> spec = PaymentTransactionSpecification
                .byType(filter.type())
                .and(PaymentTransactionSpecification.byCreatedAt(filter.fromDateAsInstant(clock), filter.toDateAsInstant(clock)))
                .and(PaymentTransactionSpecification.byAmount(filter.fromAmount(), filter.toAmount()));

        return transactionRepository.findAll(spec, pageable);
    }
}
