package com.testtask.bankcardmanagement.service.transaction;

import com.testtask.bankcardmanagement.model.dto.transaction.TransactionResponse;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionTransferRequest;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionWriteOffRequest;

public interface TransactionService {
    TransactionResponse transfer(TransactionTransferRequest transactionTransferRequest);
    TransactionResponse writeOff(TransactionWriteOffRequest transactionWriteOffRequest);
}
