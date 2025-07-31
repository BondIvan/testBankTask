package com.testtask.bankcardmanagement.service.limit.impl;

import com.testtask.bankcardmanagement.exception.limit.LimitExceededException;
import com.testtask.bankcardmanagement.model.Card;
import com.testtask.bankcardmanagement.model.Limit;
import com.testtask.bankcardmanagement.model.Transaction;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionParamFilter;
import com.testtask.bankcardmanagement.model.enums.LimitType;
import com.testtask.bankcardmanagement.model.enums.TransactionType;
import com.testtask.bankcardmanagement.repository.LimitRepository;
import com.testtask.bankcardmanagement.repository.TransactionRepository;
import com.testtask.bankcardmanagement.service.limit.LimitService;
import com.testtask.bankcardmanagement.service.security.SecurityService;
import com.testtask.bankcardmanagement.service.transaction.impl.TransactionSpecification;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LimitServiceImpl implements LimitService {
    private final TransactionRepository transactionRepository;
    private final LimitRepository limitRepository;
    private final SecurityService securityService;

    @Override
    public void checkCardLimits(Card card, BigDecimal amount) {
        Hibernate.initialize(card.getLimits());
        List<Limit> limits = card.getLimits();

        for(Limit limit: limits) {
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

    @Override
    @Transactional
    public Limit setCardLimit(Card card, LimitType limitType, BigDecimal maxAmount) {
        Optional<Limit> existingLimitByCardOptional = limitRepository.findLimitByCardIdAndLimitType(card.getId(), limitType);

        Limit changingLimit;
        if(existingLimitByCardOptional.isPresent()) {
            changingLimit = existingLimitByCardOptional.get();
            changingLimit.setMaxAmount(maxAmount);
        } else {
            changingLimit = new Limit();
            changingLimit.setLimitType(limitType);
            changingLimit.setCard(card);
            changingLimit.setMaxAmount(maxAmount);
        }

        return limitRepository.save(changingLimit);
    }

    private List<Transaction> getAllTransactionsByUserCardForADay(Long cardId) {
        Long userId = securityService.getCurrentUser().getId();
        LocalDateTime startThisDay = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
        LocalDateTime startNextDay = startThisDay.plusDays(1);
        TransactionParamFilter filter = new TransactionParamFilter(
                cardId,
                TransactionType.WRITE_OFF,
                startThisDay,
                startNextDay
        );

        Specification<Transaction> spec = TransactionSpecification.build(filter, userId);
        return transactionRepository.findAll(spec);
    }

    private List<Transaction> getAllTransactionsByUserCardForAMonth(Long cardId) {
        Long userId = securityService.getCurrentUser().getId();
        LocalDateTime startThisMonth = LocalDateTime.now().withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
        LocalDateTime startNextMonth = startThisMonth.plusMonths(1);
        TransactionParamFilter filter = new TransactionParamFilter(
                cardId,
                TransactionType.WRITE_OFF,
                startThisMonth,
                startNextMonth
        );

        Specification<Transaction> spec = TransactionSpecification.build(filter, userId);
        return transactionRepository.findAll(spec);
    }
}
