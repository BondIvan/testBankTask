package com.testtask.bankcardmanagement.service.transaction.impl;

import com.testtask.bankcardmanagement.encrypt.AESEncryption;
import com.testtask.bankcardmanagement.exception.card.CardNotFoundException;
import com.testtask.bankcardmanagement.exception.transaction.TransactionDeclinedException;
import com.testtask.bankcardmanagement.model.Card;
import com.testtask.bankcardmanagement.model.Transaction;
import com.testtask.bankcardmanagement.model.User;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionParamFilter;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionResponse;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionTransferRequest;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionWriteOffRequest;
import com.testtask.bankcardmanagement.model.enums.TransactionType;
import com.testtask.bankcardmanagement.model.mapper.TransactionMapper;
import com.testtask.bankcardmanagement.repository.CardRepository;
import com.testtask.bankcardmanagement.repository.TransactionRepository;
import com.testtask.bankcardmanagement.service.card.CardService;
import com.testtask.bankcardmanagement.service.limit.LimitService;
import com.testtask.bankcardmanagement.service.security.SecurityService;
import com.testtask.bankcardmanagement.service.transaction.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final AESEncryption aesEncryption;
    private final TransactionMapper transactionMapper;
    private final CardService cardService;
    private final LimitService limitService;
    private final SecurityService securityService;

    @Override
    @Transactional
    public TransactionResponse transfer(TransactionTransferRequest transactionTransferRequest) {
        User user = securityService.getCurrentUser();

        Card senderCard = cardService.findCardByNumber(transactionTransferRequest.fromCardNumber(), user);
        Card receiverCard = cardService.findCardByNumber(transactionTransferRequest.toCardNumber(), user);

        if(!cardService.validateCardOwnership(senderCard.getId()) || !cardService.validateCardOwnership(receiverCard.getId()))
            throw new TransactionDeclinedException("Card does not belong to the user.");

        LocalDateTime localDateTime = LocalDateTime.now();

        Transaction senderTransaction = createTransaction(
                senderCard,
                TransactionType.WRITE_OFF,
                transactionTransferRequest.amount(),
                transactionTransferRequest.description(),
                maskTargetNumber(transactionTransferRequest.toCardNumber()),
                localDateTime
        );

        Transaction receiverTransaction = createTransaction(
                receiverCard,
                TransactionType.REPLENISHMENT,
                transactionTransferRequest.amount(),
                transactionTransferRequest.description(),
                maskTargetNumber(transactionTransferRequest.fromCardNumber()),
                localDateTime
        );

        senderCard.setBalance(senderCard.getBalance().subtract(transactionTransferRequest.amount()));
        receiverCard.setBalance(receiverCard.getBalance().add(transactionTransferRequest.amount()));

        cardRepository.saveAll(List.of(senderCard, receiverCard));
        List<Transaction> savedTransactions = transactionRepository.saveAll(List.of(senderTransaction, receiverTransaction));

        return transactionMapper.toTransactionResponse(savedTransactions.get(0));
    }

    @Override
    @Transactional
    public TransactionResponse writeOff(TransactionWriteOffRequest transactionWriteOffRequest) {
        User fromUser = securityService.getCurrentUser();
        Card senderCard = cardService.findCardByNumber(transactionWriteOffRequest.fromCardNumber(), fromUser);

        if(!cardService.validateCardOwnership(senderCard.getId()))
            throw new TransactionDeclinedException("Card does not belong to the user.");

        if(!cardService.isCardAvailable(senderCard))
            throw new TransactionDeclinedException("The card is not valid.");

        limitService.checkCardLimits(senderCard, transactionWriteOffRequest.amount());

        Transaction writeOffTransaction = createTransaction(
                senderCard,
                TransactionType.WRITE_OFF,
                transactionWriteOffRequest.amount(),
                transactionWriteOffRequest.description(),
                null,
                LocalDateTime.now()
        );

        senderCard.setBalance(senderCard.getBalance().subtract(transactionWriteOffRequest.amount()));

        cardRepository.save(senderCard);
        Transaction savedTransaction = transactionRepository.save(writeOffTransaction);

        return transactionMapper.toTransactionResponse(savedTransaction);
    }

    private Transaction createTransaction(Card card, TransactionType type, BigDecimal amount,
                                          String description, String target, LocalDateTime dateTime) {
        Transaction transaction = new Transaction();
        transaction.setCard(card);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setTargetMaskedCard(target);
        transaction.setTransactionDate(dateTime);

        return transaction;
    }

    @Override
    public Page<TransactionResponse> getTransactionsByUserCard(Long cardId, TransactionParamFilter transactionParamFilter,
                                                               int page, int size,
                                                               List<String> sortList, String sortOrder) {
        if(!cardService.validateCardOwnership(cardId))
            throw new CardNotFoundException("Card does not belong to the user.");

        Long userId = securityService.getCurrentUser().getId();
        TransactionParamFilter updatedFilter = new TransactionParamFilter(
                cardId,
                transactionParamFilter.type(),
                transactionParamFilter.fromDate(),
                transactionParamFilter.toDate()
        );

        return getAllTransactionsByCard(updatedFilter, page, size, sortList, sortOrder, userId);
    }

    @Override
    public Page<TransactionResponse> getTransactionsByCard(Long cardId, TransactionParamFilter transactionParamFilter,
                                                           int page, int size,
                                                           List<String> sortList, String sortOrder) {
        if(!cardService.existById(cardId))
            throw new CardNotFoundException("Card with such id not found.");


        TransactionParamFilter updatedFilter = new TransactionParamFilter(
                cardId,
                transactionParamFilter.type(),
                transactionParamFilter.fromDate(),
                transactionParamFilter.toDate()
        );

        return getAllTransactionsByCard(updatedFilter, page, size, sortList, sortOrder, null);
    }

    private Page<TransactionResponse> getAllTransactionsByCard(TransactionParamFilter filter,
                                                               int page, int size,
                                                               List<String> sortList, String sortOrder,
                                                               Long userId) {

        List<Sort.Order> sortOrderList = createSortOrder(sortList, sortOrder);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortOrderList));

        Specification<Transaction> spec = TransactionSpecification.build(filter, userId);

        List<TransactionResponse> transactions = transactionRepository.findAll(spec, pageable).stream()
                .map(transactionMapper::toTransactionResponse)
                .toList();

        return new PageImpl<>(
                transactions,
                pageable,
                transactions.size()
        );
    }

    private String maskTargetNumber(String number) {
        return "**** **** **** " + number.substring(12);
    }

    private List<Sort.Order> createSortOrder(List<String> sortList, String sortOrder) {
        Sort.Direction sortDirection = Sort.Direction.fromString(sortOrder);
        return sortList.stream()
                .map(field -> new Sort.Order(sortDirection, field))
                .toList();
    }
}
