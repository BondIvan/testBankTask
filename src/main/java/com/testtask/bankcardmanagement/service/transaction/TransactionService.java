package com.testtask.bankcardmanagement.service.transaction;

import com.testtask.bankcardmanagement.model.dto.transaction.TransactionParamFilter;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionResponse;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionTransferRequest;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionWriteOffRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface TransactionService {
    TransactionResponse transfer(TransactionTransferRequest transactionTransferRequest);
    TransactionResponse writeOff(TransactionWriteOffRequest transactionWriteOffRequest);
    Page<TransactionResponse> getTransactionsByUserCard(Long cardId, TransactionParamFilter transactionParamFilter,
                                                        int page, int size,
                                                        List<String> sortList, String sortOrder);
    Page<TransactionResponse> getTransactionsByCard(Long cardId, TransactionParamFilter transactionParamFilter,
                                                        int page, int size,
                                                        List<String> sortList, String sortOrder);
}
