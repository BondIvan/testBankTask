package com.testtask.bankcardmanagement.service.user;

import com.testtask.bankcardmanagement.exception.security.AccessDeniedException;
import com.testtask.bankcardmanagement.model.User;
import com.testtask.bankcardmanagement.model.dto.card.CardParamFilter;
import com.testtask.bankcardmanagement.model.dto.card.CardResponse;
import com.testtask.bankcardmanagement.model.dto.card.CreateCardRequest;
import com.testtask.bankcardmanagement.model.enums.UserRole;
import com.testtask.bankcardmanagement.service.card.CardService;
import com.testtask.bankcardmanagement.service.security.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminCardService {
    private final CardService cardService;
    private final SecurityService securityService;

    public CardResponse createCard(CreateCardRequest request) {
        User currenntUser = securityService.getCurrentUser();
        if(currenntUser.getRole() != UserRole.ADMIN)
            throw new AccessDeniedException("Only admin can create card");

        return cardService.createCard(request);
    }

    public void deleteCard(Long cardId) {
        User currentUser = securityService.getCurrentUser();
        if(currentUser.getRole() != UserRole.ADMIN)
            throw new AccessDeniedException("Only admin can delete card");

        cardService.deleteCardById(cardId);
    }

    public Page<CardResponse> getAllCards(CardParamFilter filter, Pageable pageable) {
        return cardService.getAllCards(filter, pageable);
    }
}
