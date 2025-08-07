package com.testtask.bankcardmanagement.service.card;

import com.testtask.bankcardmanagement.model.Card;
import com.testtask.bankcardmanagement.model.User;
import com.testtask.bankcardmanagement.model.dto.card.CardParamFilter;
import com.testtask.bankcardmanagement.model.dto.card.CardResponse;
import com.testtask.bankcardmanagement.model.dto.card.CreateCardRequest;
import com.testtask.bankcardmanagement.model.dto.limit.LimitUpdateRequest;
import com.testtask.bankcardmanagement.model.enums.LimitType;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;

import java.util.List;

public interface CardService {
    boolean validateCardOwnership(Long cardId);
    CardResponse blockCard(Long id);
    CardResponse activateCard(Long id);
    boolean isCardAvailable(Card card);
    boolean existById(@NonNull Long cardId);
    Card findCardByNumber(String cardNumber, User owner);

    CardResponse createCard(CreateCardRequest createCardRequest);
    boolean deleteCardById(Long cardId);
    Page<CardResponse> getAllCards(CardParamFilter filter, int page, int size, List<String> sortList, String sortOrder);
    CardResponse updateCardLimit(Long userId, Long cardId, LimitUpdateRequest limitUpdateRequest);
    CardResponse removeCardLimit(Long userId, Long cardId, LimitType limitType);
//    BlockCardResponse blockCard(Long cardId);
//    ActivateCardResponse activateCard(Long cardId);
//    Boolean sendBlockRequest(Long userId, Long cardId);
//    Boolean sendActiveRequest(Long userId, Long cardId);
}
