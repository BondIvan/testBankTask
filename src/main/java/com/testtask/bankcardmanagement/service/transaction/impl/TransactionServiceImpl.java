package com.testtask.bankcardmanagement.service.transaction.impl;

import com.testtask.bankcardmanagement.encrypt.AESEncryption;
import com.testtask.bankcardmanagement.exception.card.CardBalanceException;
import com.testtask.bankcardmanagement.exception.card.CardNotFoundException;
import com.testtask.bankcardmanagement.exception.security.AccessDeniedException;
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
import com.testtask.bankcardmanagement.repository.UserRepository;
import com.testtask.bankcardmanagement.service.card.CardService;
import com.testtask.bankcardmanagement.service.transaction.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
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
    private final UserRepository userRepository;
    private final CardService cardService;

    //TODO Check limits
    //TODO Check if card status is not block

    @Override
    @Transactional
    public TransactionResponse transfer(TransactionTransferRequest transactionTransferRequest) {
        User user = getCurrentUser(transactionTransferRequest.email());

        Card senderCard = findCardByNumber(transactionTransferRequest.fromCardNumber(), user);
        Card receiverCard = findCardByNumber(transactionTransferRequest.toCardNumber(), user);

        checkCardLimits(senderCard);

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
        User fromUser = getCurrentUser(transactionWriteOffRequest.userEmail());
        Card senderCard = findCardByNumber(transactionWriteOffRequest.fromCardNumber(), fromUser);

        checkCardLimits(senderCard);

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

    @Override
    public Page<TransactionResponse> getTransactionsByUserCard(Long cardId, TransactionParamFilter transactionParamFilter,
                                                               int page, int size, List<String> sortList, String sortOrder) {
        if(!cardService.validateCardOwnership(cardId))
            throw new AccessDeniedException("Card does not belong to the user.");

        List<Sort.Order> sortOrderList = createSortOrder(sortList, sortOrder);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortOrderList));

        TransactionParamFilter updatedFilter = new TransactionParamFilter(
                cardId,
                transactionParamFilter.type(),
                transactionParamFilter.fromDate(),
                transactionParamFilter.toDate()
        );

        Specification<Transaction> transactionSpec = TransactionSpecification.build(updatedFilter);

        List<TransactionResponse> transactions = transactionRepository.findAll(transactionSpec, pageable).stream()
                .map(transactionMapper::toTransactionResponse)
                .toList();

        return new PageImpl<>(
                transactions,
                pageable,
                transactions.size()
        );
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

    private String maskTargetNumber(String number) {
        return "**** **** **** " + number.substring(12);
    }

    private void checkCardLimits(Card card) {
        //TODO Temporary solution
        if(card.getBalance().compareTo(BigDecimal.ZERO) < 0)
            throw new CardBalanceException("This card has negative balance.");
    }

    private User getCurrentUser(String userEmail) {
        //TODO Temporary solution
        return userRepository.findUserByEmail(userEmail).get();
    }

    private Card findCardByNumber(String cardNumber, User user) {
        //TODO Temporary solution
        return cardRepository.findAllByUser(user).stream()
                .filter(card -> aesEncryption.decrypt(card.getEncryptedNumber()).equals(cardNumber))
                .findFirst()
                .orElseThrow(() -> new CardNotFoundException("You don't have a card with that number - " + cardNumber + "."));
    }

    private List<Sort.Order> createSortOrder(List<String> sortList, String sortOrder) {
        Sort.Direction sortDirection = Sort.Direction.fromString(sortOrder);
        return sortList.stream()
                .map(field -> new Sort.Order(sortDirection, field))
                .toList();
    }
}
