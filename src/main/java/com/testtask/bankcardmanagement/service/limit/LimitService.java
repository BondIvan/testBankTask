package com.testtask.bankcardmanagement.service.limit;

import com.testtask.bankcardmanagement.exception.limit.LimitNotFoundException;
import com.testtask.bankcardmanagement.model.Card;
import com.testtask.bankcardmanagement.model.Limit;
import com.testtask.bankcardmanagement.model.enums.LimitType;
import com.testtask.bankcardmanagement.repository.LimitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LimitService {
    private final LimitRepository limitRepository;

    @Transactional
    public Limit setCardLimit(Card card, LimitType limitType, BigDecimal maxAmount) {
        Optional<Limit> existingLimit = card.getLimits().stream()
                .filter(limit -> limit.getLimitType() == limitType)
                .findFirst();

        Limit newLimit;
        if(existingLimit.isPresent()) {
            newLimit = existingLimit.get();
            newLimit.setMaxAmount(maxAmount);
        } else {
            newLimit = new Limit();
            newLimit.setLimitType(limitType);
            newLimit.setMaxAmount(maxAmount);
            newLimit.setCard(card);

            card.getLimits().add(newLimit);
        }

        return limitRepository.save(newLimit);
    }

    public Limit getCardLimitByLimitType(Card cardWithLimit, LimitType limitType) {
        return cardWithLimit.getLimits().stream()
                .filter(limit -> limit.getLimitType() == limitType)
                .findFirst()
                .orElseThrow(() -> new LimitNotFoundException(
                        String.format("Cannot find %s limit for card with id: [%d]", limitType, cardWithLimit.getId())
                ));
    }
}
