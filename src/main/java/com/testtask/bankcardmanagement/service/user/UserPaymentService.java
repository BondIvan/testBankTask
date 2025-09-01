package com.testtask.bankcardmanagement.service.user;

import com.testtask.bankcardmanagement.model.Card;
import com.testtask.bankcardmanagement.model.User;
import com.testtask.bankcardmanagement.model.dto.transaction.PaymentTransactionReplenishmentRequest;
import com.testtask.bankcardmanagement.model.dto.transaction.PaymentTransactionResponse;
import com.testtask.bankcardmanagement.model.dto.transaction.PaymentTransactionTransferRequest;
import com.testtask.bankcardmanagement.model.dto.transaction.PaymentTransactionWithdrawalRequest;
import com.testtask.bankcardmanagement.model.mapper.TransactionMapper;
import com.testtask.bankcardmanagement.model.transaction.ReplenishmentTransaction;
import com.testtask.bankcardmanagement.model.transaction.TransferTransaction;
import com.testtask.bankcardmanagement.model.transaction.WithdrawalTransaction;
import com.testtask.bankcardmanagement.service.card.CardService;
import com.testtask.bankcardmanagement.service.security.SecurityService;
import com.testtask.bankcardmanagement.service.transaction.PaymentOperationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserPaymentService {
    private final CardService cardService;
    private final PaymentOperationService paymentOperationService;
    private final SecurityService securityService;
    private final TransactionMapper transactionMapper;

    public PaymentTransactionResponse createReplenishmentTransaction(PaymentTransactionReplenishmentRequest request) {
        User currentUser = securityService.getCurrentUser();

        Card userCard = cardService.getCardByUserAndNumber(currentUser.getId(), request.toCardNumber());

        ReplenishmentTransaction replenishment = paymentOperationService.createReplenishmentTransaction(userCard,
                request.amount(), request.description());

        return transactionMapper.toTransactionResponse(replenishment);
    }

    public PaymentTransactionResponse createWithdrawalTransaction(PaymentTransactionWithdrawalRequest request) {
        User currentUser = securityService.getCurrentUser();

        Card userCard = cardService.getCardByUserAndNumber(currentUser.getId(), request.fromCardNumber());

        WithdrawalTransaction withdrawal = paymentOperationService.createWithdrawalTransaction(userCard,
                request.amount(), request.description());

        return transactionMapper.toTransactionResponse(withdrawal);
    }

    public PaymentTransactionResponse createTransferTransaction(PaymentTransactionTransferRequest request) {
        User currentUser = securityService.getCurrentUser();

        Card userSourceCard = cardService.getCardByUserAndNumber(currentUser.getId(), request.fromCardNumber());
        Card userTargetCard = cardService.getCardByUserAndNumber(currentUser.getId(), request.toCardNumber());

        TransferTransaction transfer = paymentOperationService.createTransferTransaction(userSourceCard, userTargetCard,
                request.amount(), request.description());

        return transactionMapper.toTransactionResponse(transfer);
    }
}
