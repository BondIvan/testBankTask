package com.testtask.bankcardmanagement.service.limit.windowStrategy;

import com.testtask.bankcardmanagement.model.enums.LimitType;

import java.time.Clock;

public interface DateWindowStrategy {
    DateRange windowForNow(Clock clock);
    LimitType getType();
}
