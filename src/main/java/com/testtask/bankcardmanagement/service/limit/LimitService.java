package com.testtask.bankcardmanagement.service.limit;

import com.testtask.bankcardmanagement.model.Card;
import com.testtask.bankcardmanagement.model.Limit;
import com.testtask.bankcardmanagement.model.enums.LimitType;

import java.math.BigDecimal;

public interface LimitService {
    void checkCardLimits(Card card, BigDecimal amount);
    Limit setCardLimit(Card card, LimitType limitType, BigDecimal maxAmount);
    Limit getCardLimitByLimitType(Card card, LimitType limitType);
}
