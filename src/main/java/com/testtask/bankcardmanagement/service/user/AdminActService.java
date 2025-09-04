package com.testtask.bankcardmanagement.service.user;

import com.testtask.bankcardmanagement.exception.card.CardNotFoundException;
import com.testtask.bankcardmanagement.exception.security.AccessDeniedException;
import com.testtask.bankcardmanagement.model.Card;
import com.testtask.bankcardmanagement.model.User;
import com.testtask.bankcardmanagement.model.dto.card.CardParamFilter;
import com.testtask.bankcardmanagement.model.dto.card.CardResponse;
import com.testtask.bankcardmanagement.model.dto.transaction.PaymentTransactionParamFilter;
import com.testtask.bankcardmanagement.model.dto.transaction.PaymentTransactionResponse;
import com.testtask.bankcardmanagement.model.enums.UserRole;
import com.testtask.bankcardmanagement.model.mapper.CardMapper;
import com.testtask.bankcardmanagement.model.mapper.TransactionMapper;
import com.testtask.bankcardmanagement.model.transaction.AbstractPaymentTransaction;
import com.testtask.bankcardmanagement.service.card.CardService;
import com.testtask.bankcardmanagement.service.security.SecurityService;
import com.testtask.bankcardmanagement.service.transaction.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminActService {
    private final SecurityService securityService;
    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;
    private final CardService cardService;
    private final CardMapper cardMapper;

    public Page<PaymentTransactionResponse> getAllTransactionsByCardId(Long cardId,
                                                                       PaymentTransactionParamFilter filer, Pageable pageable) {
        User currentUser = securityService.getCurrentUser();
        if(currentUser.getRole() != UserRole.ADMIN)
            throw new AccessDeniedException("Only admin can get transactions by any card");

        boolean isCardExist = cardService.checkIsCardExist(cardId);
        if(!isCardExist)
            throw new CardNotFoundException("There is no card with such id");

        Page<AbstractPaymentTransaction> pageTransactions =
                transactionService.getAllTransactionsByCardId(cardId, filer, pageable);

        return pageTransactions.map(transactionMapper::toTransactionResponse);
    }

    public Page<CardResponse> getAllCards(CardParamFilter filter, Pageable pageable) {
        Page<Card> pageCards = cardService.getAllCards(filter, pageable);
        return pageCards.map(cardMapper::toCardResponse);
    }
}
