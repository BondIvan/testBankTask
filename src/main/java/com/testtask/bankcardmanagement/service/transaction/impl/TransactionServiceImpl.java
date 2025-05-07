package com.testtask.bankcardmanagement.service.transaction.impl;

import com.testtask.bankcardmanagement.encrypt.AESEncryption;
import com.testtask.bankcardmanagement.exception.card.CardDuplicateException;
import com.testtask.bankcardmanagement.exception.card.CardNotFoundException;
import com.testtask.bankcardmanagement.exception.limit.LimitExceededException;
import com.testtask.bankcardmanagement.exception.transaction.TransactionDeclinedException;
import com.testtask.bankcardmanagement.exception.user.UserNotFoundException;
import com.testtask.bankcardmanagement.model.Card;
import com.testtask.bankcardmanagement.model.Limit;
import com.testtask.bankcardmanagement.model.Transaction;
import com.testtask.bankcardmanagement.model.User;
import com.testtask.bankcardmanagement.model.dto.card.CardRequest;
import com.testtask.bankcardmanagement.model.dto.card.CardResponse;
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

/**
 * Service for operations with card transactions
 * @see CardService
 * @see Card
 * @see Transaction
 * @see AESEncryption
 */
@RequiredArgsConstructor
@Service
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final AESEncryption aesEncryption;
    private final TransactionMapper transactionMapper;
    private final CardService cardService;

    /**
     * Method for transferring funds between user cards
     * @param transactionTransferRequest a request object containing the translation details
     * @return an object {@link TransactionResponse} containing information about the transaction carried out
     * @see TransactionTransferRequest
     * @see TransactionResponse
     * @throws TransactionDeclinedException If the card does not belong to the user
     */
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

    /**
     * Method for debiting funds from the user's card
     * @param transactionWriteOffRequest request object containing the write-off details
     * @return an object {@link TransactionResponse} containing information about the transaction carried out
     * @see TransactionWriteOffRequest
     * @see TransactionResponse
     * @throws TransactionDeclinedException If the card does not belong to the user or the card is not valid
     */
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

    /**
     * Method for creating a transaction object
     * @param card the card for which the transfer or debit operation is performed
     * @param type transaction type
     * @param amount the amount of funds involved in the transaction
     * @param description transaction description
     * @param target masked recipient card number
     * @param dateTime transaction time
     * @return {@link Transaction} object
     * @see TransactionWriteOffRequest
     * @see TransactionResponse
     * @see TransactionType
     */
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

    /**
     * Method to get all transactions of the specified card for the current user, checking if the card belongs to the user
     * @param cardId card id for which transactions need to be received
     * @param transactionParamFilter request object containing filter criteria
     * @param page page number
     * @param size page size
     * @param sortList list of fields to sort by
     * @param sortOrder sort direction (ASC - ascending / DESC - descending)
     * @return an object {@link Page<TransactionResponse>} representing a page of transactions
     * @see TransactionParamFilter
     * @see Page
     * @see TransactionResponse
     * @throws TransactionDeclinedException If the card does not belong to the user
     */
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

    /**
     * Method to get all transactions for a specified card
     * @param cardId card id for which transactions need to be received
     * @param transactionParamFilter request object containing filter criteria
     * @param page page number
     * @param size page size
     * @param sortList list of fields to sort by
     * @param sortOrder sort direction (ASC - ascending / DESC - descending)
     * @return an object {@link Page<TransactionResponse>} representing a page of transactions
     * @see TransactionParamFilter
     * @see Page
     * @see TransactionResponse
     * @throws TransactionDeclinedException If the card does not belong to the user
     */
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

    /**
     * Method to get all transactions on a card
     * @param filter request object containing filter criteria
     * @param page page number
     * @param size page size
     * @param sortList list of fields to sort by
     * @param sortOrder sort direction (ASC - ascending / DESC - descending)
     * @return an object {@link Page<TransactionResponse>} representing a page of transactions
     * @see TransactionParamFilter
     * @see Page
     * @see TransactionResponse
     * @see Sort.Order
     * @see TransactionSpecification
     * @throws CardNotFoundException If the card is not found
     */
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


    /**
     * Method to get all transactions of the specified card for the current user for the period: day.
     * With verification of the card's ownership by the user
     * @param cardId card id for which transactions need to be received
     * @return {@link List<Transaction>} list of transactions
     * @see TransactionParamFilter
     * @see TransactionSpecification
     */
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

    /**
     * Method to get all transactions of the specified card for the current user for the period: month.
     * With verification of the card's ownership by the user
     * @param cardId card id for which transactions need to be received
     * @return {@link List<Transaction>} list of transactions
     * @see TransactionParamFilter
     * @see TransactionSpecification
     */
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

    /**
     * The method checks whether the transaction amount exceeds the established limits for the card
     * If the limit type is {@code NO_LIMIT}, the check for this limit is skipped.
     * For limits of type {@code DAILY} and {@code MONTHLY}, the total amount of transactions
     * for the corresponding period (day or month) is calculated and compared with the maximum limit amount.
     * @param card object {@link Card}, for which limits are checked
     * @param transactionAmount the {@link BigDecimal} amount of the current transaction to be verified
     * @see Limit
     * @see LimitType
     * @throws LimitExceededException If any of the set {@code DAILY} or {@code MONTHLY} limits are exceeded
     */
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

    /**
     * The method masks the card number, leaving only the last 4 digits visible
     * @param number a string representation of the card number to be masked
     * @return {@code String} masked card number in the format "**** **** **** xxxx", where xxxx are the last 4 digits
     */
    private String maskTargetNumber(String number) {
        return "**** **** **** " + number.substring(12);
    }

    /**
     * The method finds the user's card by its full number
     * <p><b>The current implementation will be changed</b></p>
     * @param cardNumber card number as a string to find
     * @param user object {@link User}, to whom the sought card belongs
     * @return object {@link Card}
     * @throws CardNotFoundException If the card with the specified number is not found on the user's account
     */
    @Deprecated
    private Card findCardByNumber(String cardNumber, User user) {
        //TODO Temporary solution (rewrite to cardHash)
        return cardRepository.findAllByUser(user).stream()
                .filter(card -> aesEncryption.decrypt(card.getEncryptedNumber()).equals(cardNumber))
                .findFirst()
                .orElseThrow(() -> new CardNotFoundException("You don't have a card with that number - " + cardNumber + "."));
    }

    /**
     * The method creates a list of {@link Sort.Order} objects for use in sort queries.
     * @param sortList  list of fields to sort by
     * @param sortOrder sort direction (ASC - ascending / DESC - descending)
     * @return list of {@link Sort.Order}
     * @see Sort.Order
     */
    private List<Sort.Order> createSortOrder(List<String> sortList, String sortOrder) {
        Sort.Direction sortDirection = Sort.Direction.fromString(sortOrder);
        return sortList.stream()
                .map(field -> new Sort.Order(sortDirection, field))
                .toList();
    }
}
