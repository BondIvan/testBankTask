package com.testtask.bankcardmanagement.model.mapper;

import com.testtask.bankcardmanagement.model.Limit;
import com.testtask.bankcardmanagement.model.dto.limit.LimitResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LimitMapper {
    public List<LimitResponse> toListLimitResponse(List<Limit> limits) {
        return limits.stream()
                .map(this::toResponse)
                .toList();
    }

    private LimitResponse toResponse(Limit limit) {
        return new LimitResponse(
                limit.getLimitType(),
                limit.getMaxAmount()
        );
    }
}
