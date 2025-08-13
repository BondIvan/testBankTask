package com.testtask.bankcardmanagement.service.limit;

import com.testtask.bankcardmanagement.exception.limit.LimitExceededException;
import com.testtask.bankcardmanagement.exception.limit.LimitException;
import com.testtask.bankcardmanagement.model.Card;
import com.testtask.bankcardmanagement.model.Limit;
import com.testtask.bankcardmanagement.model.enums.LimitType;
import com.testtask.bankcardmanagement.repository.TransactionRepository;
import com.testtask.bankcardmanagement.service.limit.windowStrategy.DateRange;
import com.testtask.bankcardmanagement.service.limit.windowStrategy.DateWindowStrategy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LimitValidationService {
    private final Clock clock;
    private final TransactionRepository transactionRepository;
    private final Map<LimitType, DateWindowStrategy> limitTypeWindowStrategies;

    public LimitValidationService(Clock clock,
                                  TransactionRepository transactionRepository,
                                  List<DateWindowStrategy> strategies) {
        this.clock = clock;
        this.transactionRepository = transactionRepository;
        this.limitTypeWindowStrategies = strategies.stream()
                .collect(Collectors.toMap(
                        DateWindowStrategy::getType,
                        Function.identity()
                ));
    }

    public boolean areLimitsExceeded(Card cardWithLimits, BigDecimal transactionAmount) {
        for(Limit limit: cardWithLimits.getLimits())
            checkLimit(cardWithLimits, limit, transactionAmount);

        return true;
    }

    private void checkLimit(Card card, Limit limit, BigDecimal amount) {
        DateWindowStrategy window = limitTypeWindowStrategies.get(limit.getLimitType());
        Optional.ofNullable(window)
                .orElseThrow(() -> new LimitException("There is no strategy for [" + limit.getLimitType() + "]"));

        DateRange range = window.windowForNow(clock);

        BigDecimal spent = transactionRepository.findOutgoingSumTransactionsByCardIdAndPeriod(card.getId(), range.from(), range.to());
        BigDecimal amountAfterTransaction = spent.add(amount);

        if(amountAfterTransaction.compareTo(limit.getMaxAmount()) > 0) {
            throw new LimitExceededException(
                    String.format("Limit %s exceeded. Max: %s, current operation %s, already spent: %s",
                            limit.getLimitType(), limit.getMaxAmount(), amount, spent)
            );
        }
    }
}
