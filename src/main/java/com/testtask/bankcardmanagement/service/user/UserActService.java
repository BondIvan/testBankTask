package com.testtask.bankcardmanagement.service.user;

import com.testtask.bankcardmanagement.model.User;
import com.testtask.bankcardmanagement.model.dto.card.CardParamFilter;
import com.testtask.bankcardmanagement.model.dto.card.CardResponse;
import com.testtask.bankcardmanagement.model.dto.limit.LimitResponse;
import com.testtask.bankcardmanagement.model.dto.limit.LimitUpdateRequest;
import com.testtask.bankcardmanagement.model.enums.LimitType;
import com.testtask.bankcardmanagement.service.card.CardService;
import com.testtask.bankcardmanagement.service.limit.LimitService;
import com.testtask.bankcardmanagement.service.security.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class UserActService {
    private final CardService cardService;
    private final SecurityService securityService;
    private final LimitService limitService;

    public Page<CardResponse> getAllCards(CardParamFilter filter, int page, int size, List<String> sortList, String sortOrder) {
        User currentUser = getCurrentUser();

        CardParamFilter filterByCurrentUser = new CardParamFilter(
                filter.status(),
                currentUser.getEmail()
        );

        return cardService.getAllCards(filterByCurrentUser, page, size, sortList, sortOrder);
    }

    public List<LimitResponse> updateCardLimit(Long cardId, LimitUpdateRequest request) {
        User currentUser = getCurrentUser();
        return limitService.updateCardLimit(currentUser.getId(), cardId, request);
    }

    public List<LimitResponse> deleteCardLimit(Long cardId, LimitType limitType) {
        User currentUser = getCurrentUser();
        return limitService.removeCardLimit(currentUser.getId(), cardId, limitType);
    }

    private User getCurrentUser() {
        return securityService.getCurrentUser();
    }
}
