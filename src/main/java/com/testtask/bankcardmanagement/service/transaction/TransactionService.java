package com.testtask.bankcardmanagement.service.transaction;

import com.testtask.bankcardmanagement.exception.card.CardBalanceException;
import com.testtask.bankcardmanagement.exception.card.CardNotAvailableException;
import com.testtask.bankcardmanagement.exception.card.CardNotFoundException;
import com.testtask.bankcardmanagement.model.Card;
import com.testtask.bankcardmanagement.model.dto.transaction.PaymentTransactionParamFilter;
import com.testtask.bankcardmanagement.model.enums.CardStatus;
import com.testtask.bankcardmanagement.model.transaction.AbstractPaymentTransaction;
import com.testtask.bankcardmanagement.model.transaction.ReplenishmentTransaction;
import com.testtask.bankcardmanagement.model.transaction.TransferTransaction;
import com.testtask.bankcardmanagement.model.transaction.WithdrawalTransaction;
import com.testtask.bankcardmanagement.repository.CardRepository;
import com.testtask.bankcardmanagement.repository.PaymentTransactionRepository;
import com.testtask.bankcardmanagement.service.limit.LimitValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final PaymentTransactionRepository transactionRepository;
    private final Clock clock;
    private final CardRepository cardRepository;
    private final LimitValidationService limitValidationService;

    @Transactional
    public ReplenishmentTransaction createReplenishmentTransaction(Card targetCard, BigDecimal amount, String description) {
        Long targetCardId = targetCard.getId();
        Card lockTargetCard = cardRepository.findCardByIdForUpdate(targetCardId)
                .orElseThrow(() -> new CardNotFoundException("Target card not found during transaction lock."));

        if(lockTargetCard.getStatus() == CardStatus.EXPIRED)
            throw new CardNotAvailableException("Cannot make replenishment to the card because it " + lockTargetCard.getStatus());

        ReplenishmentTransaction replenishment = new ReplenishmentTransaction();
        replenishment.setTransactionNumber(UUID.randomUUID());
        replenishment.setAmount(amount);
        replenishment.setTargetCard(lockTargetCard);
        replenishment.setCreatedAt(clock.instant());
        replenishment.setDescription(description);

        lockTargetCard.setBalance(lockTargetCard.getBalance().add(amount));
        cardRepository.save(lockTargetCard);

        return transactionRepository.save(replenishment);
    }

    @Transactional
    public WithdrawalTransaction createWithdrawalTransaction(Card sourceCard, BigDecimal amount, String description) {
        Long sourceCardId = sourceCard.getId();
        Card lockSourceCard = cardRepository.findCardWithLimitsByIdForUpdate(sourceCardId)
                .orElseThrow(() -> new CardNotFoundException("Source card not found during transaction lock."));

        if(lockSourceCard.getBalance().compareTo(amount) < 0)
            throw new CardBalanceException("Not enough funds on the card for making withdrawal.");

        if(lockSourceCard.getStatus() == CardStatus.BLOCKED || lockSourceCard.getStatus() == CardStatus.EXPIRED)
            throw new CardNotAvailableException("Cannot make withdrawal because of status card is " + lockSourceCard.getStatus());

        limitValidationService.areLimitsExceeded(lockSourceCard, amount);

        WithdrawalTransaction withdrawal = new WithdrawalTransaction();

        withdrawal.setTransactionNumber(UUID.randomUUID());
        withdrawal.setAmount(amount);
        withdrawal.setSourceCard(lockSourceCard);
        withdrawal.setCreatedAt(clock.instant());
        withdrawal.setDescription(description);

        lockSourceCard.setBalance(lockSourceCard.getBalance().subtract(amount));
        cardRepository.save(lockSourceCard);

        return transactionRepository.save(withdrawal);
    }

    @Transactional
    public TransferTransaction createTransferTransaction(Card sourceCard, Card targetCard, BigDecimal amount, String description) {
        Long sourceCardId = sourceCard.getId();
        Long targetCardId = targetCard.getId();

        Card lockSourceCard = cardRepository.findCardWithLimitsByIdForUpdate(sourceCardId)
                .orElseThrow(() -> new CardNotFoundException("Source card not found during transaction lock."));
        Card lockTargetCard = cardRepository.findCardWithLimitsByIdForUpdate(targetCardId)
                .orElseThrow(() -> new CardNotFoundException("Target card not found during transaction lock."));

        if(lockSourceCard.getStatus() == CardStatus.EXPIRED || lockSourceCard.getStatus() == CardStatus.BLOCKED)
            throw new CardNotAvailableException("Cannot make transfer because of status source card is " + lockSourceCard.getStatus());

        if(lockSourceCard.getBalance().compareTo(amount) < 0)
            throw new CardBalanceException("Not enough funds on the source card for making transfer.");

        limitValidationService.areLimitsExceeded(lockSourceCard, amount);

        if(lockTargetCard.getStatus() == CardStatus.EXPIRED)
            throw new CardNotAvailableException("Cannot make transfer because of status target card is " + lockTargetCard.getStatus());

        TransferTransaction transfer = new TransferTransaction();
        transfer.setSourceCard(lockSourceCard);
        transfer.setTargetCard(lockTargetCard);
        transfer.setTransactionNumber(UUID.randomUUID());
        transfer.setAmount(amount);
        transfer.setCreatedAt(clock.instant());
        transfer.setDescription(description);

        lockSourceCard.setBalance(lockSourceCard.getBalance().subtract(amount));
        lockTargetCard.setBalance(lockTargetCard.getBalance().add(amount));

        cardRepository.saveAll(List.of(lockSourceCard, lockTargetCard));

        return transactionRepository.save(transfer);
    }

    public Page<AbstractPaymentTransaction> getAllTransactionsByCardId(Long cardId, PaymentTransactionParamFilter filter,
                                                                       Pageable pageable) {

        Specification<AbstractPaymentTransaction> spec = PaymentTransactionSpecification
                .byCardId(cardId)
                .and(PaymentTransactionSpecification.byUserId(filter.userId()))
                .and(PaymentTransactionSpecification.byCreatedAt(filter.fromDateAsInstant(clock), filter.toDateAsInstant(clock)))
                .and(PaymentTransactionSpecification.byAmount(filter.fromAmount(), filter.toAmount()));

        return transactionRepository.findAll(spec, pageable);
    }
}
