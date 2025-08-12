package com.testtask.bankcardmanagement.service.transaction;

import com.testtask.bankcardmanagement.model.mapper.TransactionMapper;
import com.testtask.bankcardmanagement.repository.CardRepository;
import com.testtask.bankcardmanagement.service.limit.LimitValidationService;
import com.testtask.bankcardmanagement.service.security.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionOperationService {
    private final LimitValidationService limitValidationService;
    private final CardRepository cardRepository;
    private final TransactionMapper transactionMapper;
    private final SecurityService securityService;

//    @Transactional
//    public TransactionResponse transfer(TransactionTransferRequest transactionTransferRequest) {
//        User user = securityService.getCurrentUser();
//
//        Card senderCard = cardService.findCardByNumber(transactionTransferRequest.fromCardNumber(), user);
//        Card receiverCard = cardService.findCardByNumber(transactionTransferRequest.toCardNumber(), user);
//
//        if(!cardService.validateCardOwnership(senderCard.getId()) || !cardService.validateCardOwnership(receiverCard.getId()))
//            throw new TransactionDeclinedException("Card does not belong to the user.");
//
//        LocalDateTime localDateTime = LocalDateTime.now();
//
//        Transaction senderTransaction = createTransaction(
//                senderCard,
//                TransactionType.WRITE_OFF,
//                transactionTransferRequest.amount(),
//                transactionTransferRequest.description(),
//                maskTargetNumber(transactionTransferRequest.toCardNumber()),
//                localDateTime
//        );
//
//        Transaction receiverTransaction = createTransaction(
//                receiverCard,
//                TransactionType.REPLENISHMENT,
//                transactionTransferRequest.amount(),
//                transactionTransferRequest.description(),
//                maskTargetNumber(transactionTransferRequest.fromCardNumber()),
//                localDateTime
//        );
//
//        senderCard.setBalance(senderCard.getBalance().subtract(transactionTransferRequest.amount()));
//        receiverCard.setBalance(receiverCard.getBalance().add(transactionTransferRequest.amount()));
//
//        cardRepository.saveAll(List.of(senderCard, receiverCard));
//        List<Transaction> savedTransactions = transactionRepository.saveAll(List.of(senderTransaction, receiverTransaction));
//
//        return transactionMapper.toTransactionResponse(savedTransactions.get(0));
//    }
//
//    @Transactional
//    public TransactionResponse writeOff(TransactionWriteOffRequest transactionWriteOffRequest) {
//        User fromUser = securityService.getCurrentUser();
//        Card senderCard = cardService.findCardByNumber(transactionWriteOffRequest.fromCardNumber(), fromUser);
//
//        if(!cardService.validateCardOwnership(senderCard.getId()))
//            throw new TransactionDeclinedException("Card does not belong to the user.");
//
//        if(!cardService.isCardAvailable(senderCard))
//            throw new TransactionDeclinedException("The card is not valid.");
//
//        limitValidationService.areLimitsExceeded(senderCard, transactionWriteOffRequest.amount());
//
//        Transaction writeOffTransaction = createTransaction(
//                senderCard,
//                TransactionType.WRITE_OFF,
//                transactionWriteOffRequest.amount(),
//                transactionWriteOffRequest.description(),
//                null,
//                LocalDateTime.now()
//        );
//
//        senderCard.setBalance(senderCard.getBalance().subtract(transactionWriteOffRequest.amount()));
//
//        cardRepository.save(senderCard);
//        Transaction savedTransaction = transactionRepository.save(writeOffTransaction);
//
//        return transactionMapper.toTransactionResponse(savedTransaction);
//    }
//
//    private String maskTargetNumber(String number) {
//        return "**** **** **** " + number.substring(12);
//    }
}
