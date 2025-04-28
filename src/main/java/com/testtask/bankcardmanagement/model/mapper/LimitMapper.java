package com.testtask.bankcardmanagement.model.mapper;

import com.testtask.bankcardmanagement.model.Limit;
import com.testtask.bankcardmanagement.model.dto.card.CardResponse;
import com.testtask.bankcardmanagement.model.dto.limit.LimitRequest;
import com.testtask.bankcardmanagement.model.dto.limit.LimitResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LimitMapper {

    public Limit toLimit(LimitRequest limitRequest) {
        Limit limit = new Limit();
        limit.setLimitType(limitRequest.type());
        limit.setMaxAmount(limitRequest.maxAmount());
        return limit;
    }

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
