package com.testtask.bankcardmanagement.service.limit;

import com.testtask.bankcardmanagement.model.Card;

import java.math.BigDecimal;

public interface LimitService {
    void checkCardLimits(Card card, BigDecimal amount);
}
