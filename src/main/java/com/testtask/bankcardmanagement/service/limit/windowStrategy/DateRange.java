package com.testtask.bankcardmanagement.service.limit.windowStrategy;

import java.time.Instant;

public record DateRange(
    Instant from,
    Instant to
) { }
