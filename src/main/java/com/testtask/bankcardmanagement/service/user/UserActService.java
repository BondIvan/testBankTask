package com.testtask.bankcardmanagement.service.user;

import com.testtask.bankcardmanagement.exception.card.CardNotFoundException;
import com.testtask.bankcardmanagement.model.Card;
import com.testtask.bankcardmanagement.model.User;
import com.testtask.bankcardmanagement.model.dto.card.CardParamFilter;
import com.testtask.bankcardmanagement.model.dto.card.CardResponse;
import com.testtask.bankcardmanagement.model.dto.limit.LimitResponse;
import com.testtask.bankcardmanagement.model.dto.limit.LimitUpdateRequest;
import com.testtask.bankcardmanagement.model.dto.transaction.PaymentTransactionParamFilter;
import com.testtask.bankcardmanagement.model.dto.transaction.PaymentTransactionResponse;
import com.testtask.bankcardmanagement.model.enums.LimitType;
import com.testtask.bankcardmanagement.model.mapper.CardMapper;
import com.testtask.bankcardmanagement.model.mapper.TransactionMapper;
import com.testtask.bankcardmanagement.model.transaction.AbstractPaymentTransaction;
import com.testtask.bankcardmanagement.service.card.CardService;
import com.testtask.bankcardmanagement.service.limit.LimitService;
import com.testtask.bankcardmanagement.service.security.SecurityService;
import com.testtask.bankcardmanagement.service.transaction.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserActService {
    private final CardService cardService;
    private final SecurityService securityService;
    private final LimitService limitService;
    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;
    private final CardMapper cardMapper;

    public Page<CardResponse> getAllCards(CardParamFilter filter, Pageable pageable) {
        User currentUser = getCurrentUser();
        CardParamFilter filterByCurrentUser = new CardParamFilter(
                filter.status(),
                currentUser.getEmail()
        );

        Page<Card> pageCards = cardService.getAllCards(filterByCurrentUser, pageable);
        
        return pageCards.map(cardMapper::toCardResponse);
    }

    @Transactional
    public List<LimitResponse> updateCardLimit(Long cardId, LimitUpdateRequest request) {
        User currentUser = getCurrentUser();
        return limitService.updateCardLimit(currentUser.getId(), cardId, request);
    }

    @Transactional
    public List<LimitResponse> deleteCardLimit(Long cardId, LimitType limitType) {
        User currentUser = getCurrentUser();
        return limitService.removeCardLimit(currentUser.getId(), cardId, limitType);
    }

    public Page<PaymentTransactionResponse> getAllTransactionsByCardId(Long cardId,
                                                                       PaymentTransactionParamFilter filter, Pageable pageable) {
        User currentUser = getCurrentUser();
        PaymentTransactionParamFilter filterByCurrentUser = new PaymentTransactionParamFilter(
                currentUser.getId(),
                filter.type(),
                filter.fromAmount(),
                filter.toAmount(),
                filter.fromDate(),
                filter.toDate()
        );

        boolean isUserHaveCard = cardService.checkIsCardBelongsToUser(currentUser.getId(), cardId);
        if(!isUserHaveCard)
            throw new CardNotFoundException("The user does not have a card with such id");

        Page<AbstractPaymentTransaction> pageTransactions =
                transactionService.getAllTransactionsByCardId(cardId, filterByCurrentUser, pageable);

        return pageTransactions.map(transactionMapper::toTransactionResponse);
    }

    public Page<PaymentTransactionResponse> getTransactionsByAllCards(PaymentTransactionParamFilter transactionFilter,
                                                                      Pageable pageable) {
        User currentUser = getCurrentUser();
        PaymentTransactionParamFilter transactionFilterByCurrentUser = new PaymentTransactionParamFilter(
                currentUser.getId(),
                transactionFilter.type(),
                transactionFilter.fromAmount(),
                transactionFilter.toAmount(),
                transactionFilter.fromDate(),
                transactionFilter.toDate()
        );

        Page<AbstractPaymentTransaction> pageTransactions =
                transactionService.getTransactionsByAllCards(transactionFilterByCurrentUser, pageable);

        return pageTransactions.map(transactionMapper::toTransactionResponse);
    }

    private User getCurrentUser() {
        return securityService.getCurrentUser();
    }
}
