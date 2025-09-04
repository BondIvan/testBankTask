package com.testtask.bankcardmanagement.service.user;

import com.testtask.bankcardmanagement.exception.security.AccessDeniedException;
import com.testtask.bankcardmanagement.model.Card;
import com.testtask.bankcardmanagement.model.User;
import com.testtask.bankcardmanagement.model.dto.card.CardResponse;
import com.testtask.bankcardmanagement.model.dto.card.CreateCardRequest;
import com.testtask.bankcardmanagement.model.enums.UserRole;
import com.testtask.bankcardmanagement.model.mapper.CardMapper;
import com.testtask.bankcardmanagement.service.card.CardService;
import com.testtask.bankcardmanagement.service.security.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminCardService {
    private final CardService cardService;
    private final SecurityService securityService;
    private final CardMapper cardMapper;

    public CardResponse createCard(CreateCardRequest request) {
        User currenntUser = securityService.getCurrentUser();
        if(currenntUser.getRole() != UserRole.ADMIN)
            throw new AccessDeniedException("Only admin can create card");

        Card newCard = cardService.createCard(request);

        return cardMapper.toCardResponse(newCard);
    }

    public void deleteCard(Long cardId) {
        User currentUser = securityService.getCurrentUser();
        if(currentUser.getRole() != UserRole.ADMIN)
            throw new AccessDeniedException("Only admin can delete card");

        cardService.deleteCardById(cardId);
    }
}
