package com.testtask.bankcardmanagement.service.user;

import com.testtask.bankcardmanagement.model.User;
import com.testtask.bankcardmanagement.model.dto.card.CardParamFilter;
import com.testtask.bankcardmanagement.model.dto.card.CardResponse;
import com.testtask.bankcardmanagement.model.dto.limit.LimitUpdateRequest;
import com.testtask.bankcardmanagement.service.card.CardService;
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

    public Page<CardResponse> getAllCards(CardParamFilter filter, int page, int size, List<String> sortList, String sortOrder) {
        User currentUser = securityService.getCurrentUser();

        CardParamFilter filterByCurrentUser = new CardParamFilter(
                filter.status(),
                currentUser.getEmail()
        );

        return cardService.getAllCards(filterByCurrentUser, page, size, sortList, sortOrder);
    }

    public CardResponse updateCardLimit(Long cardId, LimitUpdateRequest request) {
        User currentUser = securityService.getCurrentUser();
        return cardService.updateCardLimit(currentUser.getId(), cardId, request);
    }
}
