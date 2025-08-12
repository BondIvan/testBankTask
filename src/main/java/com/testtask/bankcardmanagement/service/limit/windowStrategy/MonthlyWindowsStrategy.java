package com.testtask.bankcardmanagement.service.limit.windowStrategy;

import com.testtask.bankcardmanagement.model.enums.LimitType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class MonthlyWindowsStrategy implements DateWindowStrategy {
    @Override
    public DateRange windowForNow(Clock clock) {
        LocalDate now = LocalDate.now(clock);
        ZoneId zoneId = clock.getZone();

        return new DateRange(
                now.withDayOfMonth(1).atStartOfDay(zoneId).toInstant(),
                now.plusMonths(1).atStartOfDay(zoneId).toInstant()
        );
    }

    @Override
    public LimitType getType() {
        return LimitType.MONTHLY;
    }
}
