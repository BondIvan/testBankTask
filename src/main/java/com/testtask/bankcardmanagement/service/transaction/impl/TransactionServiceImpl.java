package com.testtask.bankcardmanagement.service.transaction.impl;

import com.testtask.bankcardmanagement.encrypt.AESEncryption;
import com.testtask.bankcardmanagement.exception.card.CardNotFoundException;
import com.testtask.bankcardmanagement.exception.limit.LimitExceededException;
import com.testtask.bankcardmanagement.exception.transaction.TransactionDeclinedException;
import com.testtask.bankcardmanagement.model.Card;
import com.testtask.bankcardmanagement.model.Limit;
import com.testtask.bankcardmanagement.model.Transaction;
import com.testtask.bankcardmanagement.model.User;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionParamFilter;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionResponse;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionTransferRequest;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionWriteOffRequest;
import com.testtask.bankcardmanagement.model.enums.LimitType;
import com.testtask.bankcardmanagement.model.enums.TransactionType;
import com.testtask.bankcardmanagement.model.mapper.TransactionMapper;
import com.testtask.bankcardmanagement.repository.CardRepository;
import com.testtask.bankcardmanagement.repository.TransactionRepository;
import com.testtask.bankcardmanagement.service.card.CardService;
import com.testtask.bankcardmanagement.service.security.SecurityUtil;
import com.testtask.bankcardmanagement.service.transaction.TransactionService;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RequiredArgsConstructor
@Service
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final AESEncryption aesEncryption;
    private final TransactionMapper transactionMapper;
    private final CardService cardService;

    @Override
    @Transactional
    public TransactionResponse transfer(TransactionTransferRequest transactionTransferRequest) {
        User user = SecurityUtil.getCurrentUser();

        Card senderCard = findCardByNumber(transactionTransferRequest.fromCardNumber(), user);
        Card receiverCard = findCardByNumber(transactionTransferRequest.toCardNumber(), user);

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
        User fromUser = SecurityUtil.getCurrentUser();
        Card senderCard = findCardByNumber(transactionWriteOffRequest.fromCardNumber(), fromUser);

        if(!cardService.validateCardOwnership(senderCard.getId()))
            throw new TransactionDeclinedException("Card does not belong to the user.");

        if(!cardService.isCardAvailable(senderCard))
            throw new TransactionDeclinedException("The card is not valid.");

        checkCardLimits(senderCard, transactionWriteOffRequest.amount());

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
            throw new TransactionDeclinedException("Card does not belong to the user.");

        TransactionParamFilter updatedFilter = new TransactionParamFilter(
                cardId,
                transactionParamFilter.type(),
                transactionParamFilter.fromDate(),
                transactionParamFilter.toDate(),
                true
        );

        return getAllTransactionsByCard(updatedFilter, page, size, sortList, sortOrder);
    }

    @Override
    public Page<TransactionResponse> getTransactionsByCard(Long cardId, TransactionParamFilter transactionParamFilter,
                                                           int page, int size,
                                                           List<String> sortList, String sortOrder) {

        TransactionParamFilter updatedFilter = new TransactionParamFilter(
                cardId,
                transactionParamFilter.type(),
                transactionParamFilter.fromDate(),
                transactionParamFilter.toDate(),
                false
        );

        return getAllTransactionsByCard(updatedFilter, page, size, sortList, sortOrder);
    }

    private Page<TransactionResponse> getAllTransactionsByCard(TransactionParamFilter filter,
                                                               int page, int size,
                                                               List<String> sortList, String sortOrder) {

        if(!cardService.existById(filter.cardId()))
            throw new CardNotFoundException("The card with such id not found");

        List<Sort.Order> sortOrderList = createSortOrder(sortList, sortOrder);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortOrderList));

        Specification<Transaction> spec = TransactionSpecification.build(filter);

        List<TransactionResponse> transactions = transactionRepository.findAll(spec, pageable).stream()
                .map(transactionMapper::toTransactionResponse)
                .toList();

        return new PageImpl<>(
                transactions,
                pageable,
                transactions.size()
        );
    }

    private List<Transaction> getAllTransactionsByUserCardForADay(Long cardId) {
        LocalDateTime startThisDay = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
        LocalDateTime startNextDay = startThisDay.plusDays(1);
        TransactionParamFilter filter = new TransactionParamFilter(
                cardId,
                TransactionType.WRITE_OFF,
                startThisDay,
                startNextDay,
                true
        );

        Specification<Transaction> spec = TransactionSpecification.build(filter);
        return transactionRepository.findAll(spec);
    }

    private List<Transaction> getAllTransactionsByUserCardForAMonth(Long cardId) {
        LocalDateTime startThisMonth = LocalDateTime.now().withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
        LocalDateTime startNextMonth = startThisMonth.plusMonths(1);
        TransactionParamFilter filter = new TransactionParamFilter(
                cardId,
                TransactionType.WRITE_OFF,
                startThisMonth,
                startNextMonth,
                true
        );

        Specification<Transaction> spec = TransactionSpecification.build(filter);
        return transactionRepository.findAll(spec);
    }

    private void checkCardLimits(Card card, BigDecimal transactionAmount) {
        Hibernate.initialize(card.getLimits());
        List<Limit> limits = card.getLimits();

        for(Limit limit: limits) {
            if (limit.getLimitType() == LimitType.NO_LIMIT)
                continue;

            switch (limit.getLimitType()) {
                case DAILY -> {
                    BigDecimal sumForADay = getAllTransactionsByUserCardForADay(card.getId()).stream()
                            .map(Transaction::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal sumAfterDayTransactions = sumForADay.add(transactionAmount);

                    if(sumAfterDayTransactions.compareTo(limit.getMaxAmount()) > 0) {
                        throw new LimitExceededException(
                                String.format("Limit %s exceeded. Max: %s, current operation %s, already spent: %s",
                                        LimitType.DAILY, limit.getMaxAmount(), transactionAmount, sumForADay)
                        );
                    }
                }

                case MONTHLY -> {
                    BigDecimal sumForAMonth = getAllTransactionsByUserCardForAMonth(card.getId()).stream()
                            .map(Transaction::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal sumAfterTransactionMonthTransactions = sumForAMonth.add(transactionAmount);

                    if(sumAfterTransactionMonthTransactions.compareTo(limit.getMaxAmount()) > 0) {
                        throw new LimitExceededException(
                                String.format("Limit %s exceeded. Max: %s, current operation %s, already spent: %s",
                                        LimitType.MONTHLY, limit.getMaxAmount(), transactionAmount, sumForAMonth)
                        );
                    }
                }
            }
        }
    }

    private String maskTargetNumber(String number) {
        return "**** **** **** " + number.substring(12);
    }

    private Card findCardByNumber(String cardNumber, User user) {
        //TODO Temporary solution (rewrite to cardHash)
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
