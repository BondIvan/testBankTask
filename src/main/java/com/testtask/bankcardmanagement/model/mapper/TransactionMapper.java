package com.testtask.bankcardmanagement.model.mapper;

import com.testtask.bankcardmanagement.model.Transaction;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class TransactionMapper {
    private final CardMapper cardMapper;

    public TransactionResponse toTransactionResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getAmount(),
                transaction.getType(),
                cardMapper.toCardResponse(transaction.getCard()),
                transaction.getTargetMaskedCard(),
                transaction.getTransactionDate(),
                transaction.getDescription()
        );
    }
}
