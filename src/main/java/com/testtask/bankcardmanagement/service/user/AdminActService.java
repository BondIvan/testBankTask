package com.testtask.bankcardmanagement.service.user;

import com.testtask.bankcardmanagement.exception.security.AccessDeniedException;
import com.testtask.bankcardmanagement.model.User;
import com.testtask.bankcardmanagement.model.dto.auth.AuthenticationResponse;
import com.testtask.bankcardmanagement.model.dto.auth.RegistrationRequest;
import com.testtask.bankcardmanagement.model.dto.card.CardParamFilter;
import com.testtask.bankcardmanagement.model.dto.card.CardResponse;
import com.testtask.bankcardmanagement.model.dto.card.CreateCardRequest;
import com.testtask.bankcardmanagement.model.enums.UserRole;
import com.testtask.bankcardmanagement.service.card.CardService;
import com.testtask.bankcardmanagement.service.security.SecurityService;
import com.testtask.bankcardmanagement.service.security.jwt.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AdminActService {
    private final AuthenticationService authenticationService;
    private final CardService cardService;
    private final SecurityService securityService;

    public AuthenticationResponse createUser(RegistrationRequest registrationRequest) {
        User currentUser = securityService.getCurrentUser();
        if(currentUser.getRole() != UserRole.ADMIN)
            throw new AccessDeniedException("Only admin can create a user");

        return authenticationService.register(registrationRequest);
    }

    public Boolean deleteUser(Long userId) {
        return null;
    }

    public CardResponse createCard(CreateCardRequest request) {
        return cardService.createCard(request);
    }

    public void deleteCard(Long cardId) {
        User currentUser = securityService.getCurrentUser();
        if(currentUser.getRole() != UserRole.ADMIN)
            throw new AccessDeniedException("Only admin can delete card");

        cardService.deleteCardById(cardId);
    }

    public Page<CardResponse> getAllCards(CardParamFilter filter, int page, int size, List<String> sortList, String sortOrder) {
        return cardService.getAllCards(filter, page, size, sortList, sortOrder);
    }
}
