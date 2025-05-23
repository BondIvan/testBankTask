package com.testtask.bankcardmanagement.service.card;

import com.testtask.bankcardmanagement.model.Card;
import com.testtask.bankcardmanagement.model.User;
import com.testtask.bankcardmanagement.model.dto.card.CardParamFilter;
import com.testtask.bankcardmanagement.model.dto.card.CardRequest;
import com.testtask.bankcardmanagement.model.dto.card.CardResponse;
import com.testtask.bankcardmanagement.model.dto.limit.LimitUpdateRequest;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;

import java.util.List;

public interface CardService {
    CardResponse createCard(CardRequest cardRequest);
    Page<CardResponse> getAllCards(CardParamFilter cardParamFilter, int page, int size, List<String> sortList, String sortOrder);
    Page<CardResponse> getAllCardsForCurrentUser(CardParamFilter cardParamFilter, int page, int size);
    CardResponse blockCard(Long id);
    CardResponse activateCard(Long id);
    void deleteCard(Long id);
    boolean validateCardOwnership(Long cardId);
    boolean isCardAvailable(Card card);
    CardResponse updateCardLimit(Long cardId, LimitUpdateRequest limitUpdateRequest);
    boolean existById(@NonNull Long cardId);
    Card findCardByNumber(String cardNumber, User owner);
}
