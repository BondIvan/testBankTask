package com.testtask.bankcardmanagement.service.limit;

import com.testtask.bankcardmanagement.exception.limit.LimitNotFoundException;
import com.testtask.bankcardmanagement.model.Card;
import com.testtask.bankcardmanagement.model.Limit;
import com.testtask.bankcardmanagement.model.dto.limit.LimitResponse;
import com.testtask.bankcardmanagement.model.dto.limit.LimitUpdateRequest;
import com.testtask.bankcardmanagement.model.enums.LimitType;
import com.testtask.bankcardmanagement.model.mapper.LimitMapper;
import com.testtask.bankcardmanagement.repository.LimitRepository;
import com.testtask.bankcardmanagement.service.card.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LimitService {
    private final LimitRepository limitRepository;
    private final CardService cardService;
    private final LimitMapper limitMapper;

    @Transactional
    public Limit setCardLimit(Card cardWithLimits, LimitType limitType, BigDecimal maxAmount) {
        Optional<Limit> existingLimit = findExistingLimit(cardWithLimits, limitType);

        if(existingLimit.isPresent())
            return updateExistingLimit(existingLimit.get(), maxAmount);
        else
            return createLimit(cardWithLimits, limitType, maxAmount);
    }

    private Optional<Limit> findExistingLimit(Card card, LimitType type) {
        return card.getLimits().stream()
                .filter(limit -> limit.getLimitType() == type)
                .findFirst();
    }

    private Limit createLimit(Card card, LimitType limitType, BigDecimal maxAmount) {
        Limit newLimit = new Limit();
        newLimit.setLimitType(limitType);
        newLimit.setMaxAmount(maxAmount);
        newLimit.setCard(card);

        card.getLimits().add(newLimit);

        return limitRepository.save(newLimit);
    }

    private Limit updateExistingLimit(Limit existingLimit, BigDecimal maxAmount) {
        existingLimit.setMaxAmount(maxAmount);
        return limitRepository.save(existingLimit);
    }

    public Limit getCardLimitByLimitType(Card cardWithLimit, LimitType limitType) {
        return cardWithLimit.getLimits().stream()
                .filter(limit -> limit.getLimitType() == limitType)
                .findFirst()
                .orElseThrow(() -> new LimitNotFoundException(
                        String.format("Cannot find %s limit for card with id: [%d]", limitType, cardWithLimit.getId())
                ));
    }

    @Transactional
    public List<LimitResponse> updateCardLimit(Long userId, Long cardId, LimitUpdateRequest limitUpdateRequest) {
        Card cardWithLimits = cardService.getCardWithLimitsByUser(userId, cardId);

        setCardLimit(cardWithLimits, limitUpdateRequest.type(), limitUpdateRequest.maxAmount());

        return limitMapper.toListLimitResponse(cardWithLimits.getLimits());
    }

    @Transactional
    public List<LimitResponse> removeCardLimit(Long userId, Long cardId, LimitType limitType) {
        Card cardWithLimits = cardService.getCardWithLimitsByUser(userId, cardId);

        Limit removingLimit = getCardLimitByLimitType(cardWithLimits, limitType);

        cardWithLimits.getLimits().remove(removingLimit);

        return limitMapper.toListLimitResponse(cardWithLimits.getLimits());
    }
}
