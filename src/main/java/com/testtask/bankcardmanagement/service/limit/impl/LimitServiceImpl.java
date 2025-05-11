package com.testtask.bankcardmanagement.service.limit.impl;

import com.testtask.bankcardmanagement.encrypt.AESEncryption;
import com.testtask.bankcardmanagement.exception.limit.LimitExceededException;
import com.testtask.bankcardmanagement.model.Card;
import com.testtask.bankcardmanagement.model.Limit;
import com.testtask.bankcardmanagement.model.Transaction;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionParamFilter;
import com.testtask.bankcardmanagement.model.enums.LimitType;
import com.testtask.bankcardmanagement.model.enums.TransactionType;
import com.testtask.bankcardmanagement.repository.TransactionRepository;
import com.testtask.bankcardmanagement.service.card.CardService;
import com.testtask.bankcardmanagement.service.limit.LimitService;
import com.testtask.bankcardmanagement.service.transaction.impl.TransactionSpecification;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Service for working with card limits
 * @see Limit
 * @see LimitType
 * @see Card
 */
@Service
@RequiredArgsConstructor
public class LimitServiceImpl implements LimitService {
    private final TransactionRepository transactionRepository;

    /**
     * The method checks whether the transaction amount exceeds the established limits for the card
     * If the limit type is {@code NO_LIMIT}, the check for this limit is skipped.
     * For limits of type {@code DAILY} and {@code MONTHLY}, the total amount of transactions
     * for the corresponding period (day or month) is calculated and compared with the maximum limit amount.
     * @param card object {@link Card}, for which limits are checked
     * @param amount the {@link BigDecimal} amount of the current transaction to be verified
     * @see Limit
     * @see LimitType
     * @throws LimitExceededException If any of the set {@code DAILY} or {@code MONTHLY} limits are exceeded
     */
    @Override
    public void checkCardLimits(Card card, BigDecimal amount) {
        Hibernate.initialize(card.getLimits());
        List<Limit> limits = card.getLimits();

        for(Limit limit: limits) {
            if (limit.getLimitType() == LimitType.NO_LIMIT)
                continue;

            switch (limit.getLimitType()) {
                case DAILY -> {
                    BigDecimal sumForADay = getAllTransactionsByUserCardForADay(card.getId()).stream()
                            .map(Transaction::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal sumAfterDayTransactions = sumForADay.add(amount);

                    if(sumAfterDayTransactions.compareTo(limit.getMaxAmount()) > 0) {
                        throw new LimitExceededException(
                                String.format("Limit %s exceeded. Max: %s, current operation %s, already spent: %s",
                                        LimitType.DAILY, limit.getMaxAmount(), amount, sumForADay)
                        );
                    }
                }

                case MONTHLY -> {
                    BigDecimal sumForAMonth = getAllTransactionsByUserCardForAMonth(card.getId()).stream()
                            .map(Transaction::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal sumAfterTransactionMonthTransactions = sumForAMonth.add(amount);

                    if(sumAfterTransactionMonthTransactions.compareTo(limit.getMaxAmount()) > 0) {
                        throw new LimitExceededException(
                                String.format("Limit %s exceeded. Max: %s, current operation %s, already spent: %s",
                                        LimitType.MONTHLY, limit.getMaxAmount(), amount, sumForAMonth)
                        );
                    }
                }
            }
        }
    }

    /**
     * Method to get all transactions of the specified card for the current user for the period: day.
     * With verification of the card's ownership by the user
     * @param cardId card id for which transactions need to be received
     * @return {@link List<Transaction>} list of transactions
     * @see TransactionParamFilter
     * @see TransactionSpecification
     */
    private List<Transaction> getAllTransactionsByUserCardForADay(Long cardId) {
        LocalDateTime startThisDay = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
        LocalDateTime startNextDay = startThisDay.plusDays(1);
        TransactionParamFilter filter = new TransactionParamFilter(
                cardId,
                TransactionType.WRITE_OFF,
                startThisDay,
                startNextDay,
                true
        );

        Specification<Transaction> spec = TransactionSpecification.build(filter);
        return transactionRepository.findAll(spec);
    }

    /**
     * Method to get all transactions of the specified card for the current user for the period: month.
     * With verification of the card's ownership by the user
     * @param cardId card id for which transactions need to be received
     * @return {@link List<Transaction>} list of transactions
     * @see TransactionParamFilter
     * @see TransactionSpecification
     */
    private List<Transaction> getAllTransactionsByUserCardForAMonth(Long cardId) {
        LocalDateTime startThisMonth = LocalDateTime.now().withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
        LocalDateTime startNextMonth = startThisMonth.plusMonths(1);
        TransactionParamFilter filter = new TransactionParamFilter(
                cardId,
                TransactionType.WRITE_OFF,
                startThisMonth,
                startNextMonth,
                true
        );

        Specification<Transaction> spec = TransactionSpecification.build(filter);
        return transactionRepository.findAll(spec);
    }
}
