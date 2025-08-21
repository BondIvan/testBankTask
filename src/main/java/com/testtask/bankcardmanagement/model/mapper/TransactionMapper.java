package com.testtask.bankcardmanagement.model.mapper;

import com.testtask.bankcardmanagement.model.dto.transaction.TransactionResponse;
import com.testtask.bankcardmanagement.model.enums.TransactionType;
import com.testtask.bankcardmanagement.model.transaction.AbstractPaymentTransaction;
import com.testtask.bankcardmanagement.model.transaction.ReplenishmentTransaction;
import com.testtask.bankcardmanagement.model.transaction.TransferTransaction;
import com.testtask.bankcardmanagement.model.transaction.WithdrawalTransaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Component
public class TransactionMapper {
    private Clock clock;

    public TransactionResponse toTransactionResponse(AbstractPaymentTransaction transaction) {
        TransactionType type = null;
        Long sourceCardId = null;
        Long targetCardId = null;

        if(transaction instanceof ReplenishmentTransaction replenishment) {
            type = TransactionType.REPLENISHMENT;
            targetCardId = replenishment.getTargetCard().getId();
        } else if(transaction instanceof WithdrawalTransaction withdrawal) {
            type = TransactionType.WITHDRAWAL;
            sourceCardId = withdrawal.getSourceCard().getId();
        } else if(transaction instanceof TransferTransaction transfer) {
            type = TransactionType.TRANSFER;
            sourceCardId = transfer.getSourceCard().getId();
            targetCardId = transfer.getTargetCard().getId();
        }

        return new TransactionResponse(
                transaction.getAmount(),
                type,
                sourceCardId,
                targetCardId,
                transaction.getTransferGroupId(),
                LocalDateTime.ofInstant(transaction.getCreatedAt(), clock.getZone()),
                transaction.getDescription()
        );
    }
}
