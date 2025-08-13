package com.testtask.bankcardmanagement.model.mapper;

import com.testtask.bankcardmanagement.model.Transaction;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class TransactionMapper {
    private final Clock clock;

    public TransactionResponse toTransactionResponse(Transaction transaction) {
        Long sourceCardId = transaction.getSourceCard().getId();
        Long targetCardId = (transaction.getTargetCard() != null) ? transaction.getTargetCard().getId() : null;
        UUID transactionGroupId = (transaction.getTransferGroupId() != null) ? transaction.getTransferGroupId() : null;
        LocalDateTime createAt = LocalDateTime.ofInstant(transaction.getCreatedAt(), clock.getZone());

        return new TransactionResponse(
                transaction.getAmount(),
                transaction.getType(),
                transaction.getDirection(),
                sourceCardId,
                targetCardId,
                transactionGroupId,
                createAt,
                transaction.getDescription()
        );
    }
}
