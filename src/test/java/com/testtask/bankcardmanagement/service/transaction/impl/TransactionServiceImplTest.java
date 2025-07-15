package com.testtask.bankcardmanagement.service.transaction.impl;

import com.testtask.bankcardmanagement.exception.transaction.TransactionDeclinedException;
import com.testtask.bankcardmanagement.model.Card;
import com.testtask.bankcardmanagement.model.Limit;
import com.testtask.bankcardmanagement.model.Transaction;
import com.testtask.bankcardmanagement.model.User;
import com.testtask.bankcardmanagement.model.dto.card.CardResponse;
import com.testtask.bankcardmanagement.model.dto.limit.LimitResponse;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionParamFilter;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionResponse;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionTransferRequest;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionWriteOffRequest;
import com.testtask.bankcardmanagement.model.dto.user.UserResponse;
import com.testtask.bankcardmanagement.model.enums.CardStatus;
import com.testtask.bankcardmanagement.model.enums.LimitType;
import com.testtask.bankcardmanagement.model.enums.TransactionType;
import com.testtask.bankcardmanagement.model.mapper.TransactionMapper;
import com.testtask.bankcardmanagement.repository.CardRepository;
import com.testtask.bankcardmanagement.repository.TransactionRepository;
import com.testtask.bankcardmanagement.service.card.impl.CardServiceImpl;
import com.testtask.bankcardmanagement.service.limit.impl.LimitServiceImpl;
import com.testtask.bankcardmanagement.service.security.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {
    @Mock private TransactionRepository transactionRepository;
    @Mock private CardRepository cardRepository;
    @Mock private TransactionMapper transactionMapper;
    @Mock private CardServiceImpl cardService;
    @Mock private LimitServiceImpl limitService;
    @InjectMocks private TransactionServiceImpl underTest;

    private User user;
    private Card card1;
    private Card card2;
    private Limit limit1;
    private Limit limit2;
    private final LocalDate cardCreatedAt = LocalDate.of(2025, 3, 3);

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        card1 = new Card();
        card1.setId(1L);
        card1.setBalance(new BigDecimal("200"));
        card1.setStatus(CardStatus.ACTIVE);
        card1.setExpirationDate(cardCreatedAt);
        card1.setEncryptedNumber("card_1_encrypted_number");

        limit1 = new Limit();
        limit1.setId(1L);
        limit1.setCard(card1);
        limit1.setLimitType(LimitType.DAILY);
        limit1.setMaxAmount(new BigDecimal("1000"));

        card1.setLimits(List.of(limit1));
        card1.setUser(user);

        card2 = new Card();
        card2.setId(2L);
        card2.setBalance(new BigDecimal("400"));
        card2.setStatus(CardStatus.ACTIVE);
        card2.setExpirationDate(cardCreatedAt);
        card2.setEncryptedNumber("card_2_encrypted_number");

        limit2 = new Limit();
        limit2.setId(2L);
        limit2.setCard(card2);
        limit2.setLimitType(LimitType.NO_LIMIT);

        card2.setLimits(List.of(limit2));
        card2.setUser(user);

        user.setCards(List.of(card1, card2));
    }

    @Test
    void writeOff_whenCardBelongsToUserAndAvailable_shouldReturnTransactionResponseDto() {
        LocalDateTime transactionDateTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0);
        try(
                MockedStatic<SecurityUtil> secureUtil = mockStatic(SecurityUtil.class);
                MockedStatic<LocalDateTime> mockedDateTime = mockStatic(LocalDateTime.class)
        ) {
            // Given
            // sender - card2
            BigDecimal amount = new BigDecimal("50");
            BigDecimal expectedCardBalance = card2.getBalance().subtract(amount);
            String searchedCardNumber = "searchedCardNumber";
            String transactionDescription = "Transaction description";

            TransactionWriteOffRequest transactionWriteOffRequest = new TransactionWriteOffRequest(
                    searchedCardNumber,
                    amount,
                    transactionDescription
            );

            Transaction expectedTransaction = new Transaction();
            expectedTransaction.setCard(card2);
            expectedTransaction.setType(TransactionType.WRITE_OFF);
            expectedTransaction.setAmount(amount);
            expectedTransaction.setTransactionDate(transactionDateTime);
            expectedTransaction.setDescription(transactionDescription);

            TransactionResponse expectedTransactionResponse = new TransactionResponse(
              amount,
              TransactionType.WRITE_OFF,
              new CardResponse(
                      "**** **** **** 2222",
                      LocalDate.now().plusYears(1),
                      new UserResponse(null, null),
                      card2.getStatus(),
                      expectedCardBalance,
                      List.of(new LimitResponse(limit2.getLimitType(), limit2.getMaxAmount()))),
              null,
              transactionDateTime,
              transactionDescription
            );

            secureUtil.when(SecurityUtil::getCurrentUser).thenReturn(user);
            mockedDateTime.when(LocalDateTime::now).thenReturn(transactionDateTime);

            when(cardService.findCardByNumber(searchedCardNumber, user)).thenReturn(card2);
            when(cardService.validateCardOwnership(card2.getId())).thenReturn(true);
            when(cardService.isCardAvailable(card2)).thenReturn(true);
            doNothing().when(limitService).checkCardLimits(card2, amount);
            when(cardRepository.save(card2)).thenReturn(card2);
            when(transactionRepository.save(any(Transaction.class))).thenReturn(expectedTransaction);
            when(transactionMapper.toTransactionResponse(expectedTransaction)).thenReturn(expectedTransactionResponse);

            // When
            TransactionResponse actualTransactionResponse = underTest.writeOff(transactionWriteOffRequest);

            // Then
            ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
            verify(transactionRepository).save(transactionCaptor.capture());
            Transaction actualTransaction = transactionCaptor.getValue();

            assertThat(actualTransaction.getCard()).isEqualTo(expectedTransaction.getCard());
            assertThat(actualTransaction.getTransactionDate()).isEqualTo(expectedTransaction.getTransactionDate());
            assertThat(actualTransaction.getAmount()).isEqualByComparingTo(expectedTransaction.getAmount());
            assertThat(actualTransaction.getType()).isEqualTo(expectedTransaction.getType());
            assertThat(actualTransaction.getTargetMaskedCard()).isEqualTo(expectedTransaction.getTargetMaskedCard());
            assertThat(actualTransaction.getDescription()).isEqualTo(expectedTransaction.getDescription());

            assertThat(actualTransactionResponse.card()).isEqualTo(expectedTransactionResponse.card());
            assertThat(actualTransactionResponse.amount()).isEqualByComparingTo(expectedTransactionResponse.amount());
            assertThat(actualTransactionResponse.type()).isEqualTo(expectedTransactionResponse.type());
            assertThat(actualTransactionResponse.datetime()).isEqualTo(expectedTransactionResponse.datetime());
            assertThat(actualTransactionResponse.targetCard()).isEqualTo(expectedTransactionResponse.targetCard());
            assertThat(actualTransactionResponse.description()).isEqualTo(expectedTransactionResponse.description());

            verify(cardService).findCardByNumber(searchedCardNumber, user);
            verify(cardService).validateCardOwnership(card2.getId());
            verify(cardService).isCardAvailable(card2);
            verify(limitService).checkCardLimits(card2, amount);
            verify(cardRepository).save(card2);
            verify(transactionMapper).toTransactionResponse(expectedTransaction);
        }
    }

    @Test
    void writeOff_whenCardBelongsToUserAndUnavailable_shouldThrowTransactionDeclinedException() {
        try (MockedStatic<SecurityUtil> secureUtil = mockStatic(SecurityUtil.class)) {
            // Given
            // sender - card1
            BigDecimal amount = new BigDecimal("10");
            String searchedCardNumber = "searchedCardNumber";

            TransactionWriteOffRequest transactionWriteOffRequest = new TransactionWriteOffRequest(
                    searchedCardNumber,
                    amount,
                    null
            );

            secureUtil.when(SecurityUtil::getCurrentUser).thenReturn(user);
            when(cardService.findCardByNumber(searchedCardNumber, user)).thenReturn(card1);
            when(cardService.validateCardOwnership(card1.getId())).thenReturn(true);
            when(cardService.isCardAvailable(card1)).thenReturn(false);

            // When
            TransactionDeclinedException exception = assertThrows(
                    TransactionDeclinedException.class,
                    () -> underTest.writeOff(transactionWriteOffRequest)
            );

            // Then
            assertThat(exception).hasMessage("The card is not valid.");

            verify(cardService).findCardByNumber(searchedCardNumber, user);
            verify(cardService).validateCardOwnership(card1.getId());
            verify(cardService).isCardAvailable(card1);
            verifyNoMoreInteractions(cardRepository);
            verifyNoInteractions(transactionRepository);
            verifyNoInteractions(transactionMapper);
        }
    }

    @Test
    void writeOff_whenCardAvailableAndDoesntBelongsToUser_shouldThrowTransactionDeclinedException() {
        try (MockedStatic<SecurityUtil> secureUtil = mockStatic(SecurityUtil.class)) {
            // Given
            // sender - card1
            BigDecimal amount = new BigDecimal("20");
            String searchedCardNumber = "searchedCardNumber";

            TransactionWriteOffRequest transactionWriteOffRequest = new TransactionWriteOffRequest(
                    searchedCardNumber,
                    amount,
                    null
            );

            secureUtil.when(SecurityUtil::getCurrentUser).thenReturn(user);
            when(cardService.findCardByNumber(searchedCardNumber, user)).thenReturn(card1);
            when(cardService.validateCardOwnership(card1.getId())).thenReturn(false);

            // When
            TransactionDeclinedException exception = assertThrows(
                    TransactionDeclinedException.class,
                    () -> underTest.writeOff(transactionWriteOffRequest)
            );

            // Then
            assertThat(exception).hasMessage("Card does not belong to the user.");

            verify(cardService).findCardByNumber(searchedCardNumber, user);
            verify(cardService).validateCardOwnership(card1.getId());
            verifyNoMoreInteractions(cardRepository);
            verifyNoMoreInteractions(cardService);
            verifyNoInteractions(transactionRepository);
            verifyNoInteractions(transactionMapper);
        }
    }

    @Test
    void transfer_whenSenderAndReceiverCardBelongsToUser_shouldReturnTransactionResponseDto() {
        LocalDateTime transactionDateTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0);
        try(
                MockedStatic<SecurityUtil> secureUtil = mockStatic(SecurityUtil.class);
                MockedStatic<LocalDateTime> mockDateTime = mockStatic(LocalDateTime.class)
        ) {
            // Given
            Card senderCard = card1;
            Card receiverCard = card2;
            String senderCardNumber = "1111222233334444";
            String receiverCardNumber = "5555666677778888";
            String maskedSenderCardNumber = "**** **** **** " + senderCardNumber.substring(12);
            String maskedReceiverCardNumber = "**** **** **** " + receiverCardNumber.substring(12);

            BigDecimal amount = new BigDecimal("150");
            BigDecimal expectedSenderCardBalance = card1.getBalance().subtract(amount);
            BigDecimal expectedReceiverCardBalance = card2.getBalance().add(amount);

            String transactionDescription = "transaction description";

            TransactionTransferRequest transactionTransferRequest = new TransactionTransferRequest(
                    senderCardNumber,
                    receiverCardNumber,
                    amount,
                    transactionDescription
            );

            Transaction expectedSenderTransaction = new Transaction();
            expectedSenderTransaction.setCard(card1);
            expectedSenderTransaction.setType(TransactionType.WRITE_OFF);
            expectedSenderTransaction.setTransactionDate(transactionDateTime);
            expectedSenderTransaction.setAmount(amount);
            expectedSenderTransaction.setDescription(transactionDescription);
            expectedSenderTransaction.setTargetMaskedCard(maskedReceiverCardNumber);

            Transaction expectedReceiverTransaction = new Transaction();
            expectedReceiverTransaction.setCard(card2);
            expectedReceiverTransaction.setType(TransactionType.REPLENISHMENT);
            expectedReceiverTransaction.setTransactionDate(transactionDateTime);
            expectedReceiverTransaction.setAmount(amount);
            expectedReceiverTransaction.setDescription(transactionDescription);
            expectedReceiverTransaction.setTargetMaskedCard(maskedSenderCardNumber);

            TransactionResponse expectedSenderTransactionResponse = new TransactionResponse(
                    amount,
                    TransactionType.WRITE_OFF,
                    new CardResponse(
                            maskedSenderCardNumber,
                            LocalDate.now().plusYears(1),
                            new UserResponse(null, null),
                            card1.getStatus(),
                            expectedSenderCardBalance,
                            List.of(new LimitResponse(limit1.getLimitType(), limit1.getMaxAmount()))),
                    maskedReceiverCardNumber,
                    transactionDateTime,
                    transactionDescription
            );

            secureUtil.when(SecurityUtil::getCurrentUser).thenReturn(user);
            mockDateTime.when(LocalDateTime::now).thenReturn(transactionDateTime);

            when(cardService.findCardByNumber(senderCardNumber, user)).thenReturn(senderCard);
            when(cardService.findCardByNumber(receiverCardNumber, user)).thenReturn(receiverCard);
            when(cardService.validateCardOwnership(senderCard.getId())).thenReturn(true);
            when(cardService.validateCardOwnership(receiverCard.getId())).thenReturn(true);
            when(cardRepository.saveAll(List.of(senderCard, receiverCard))).thenReturn(List.of(senderCard, receiverCard));
            when(transactionRepository.saveAll(anyList())).thenReturn(List.of(expectedSenderTransaction, expectedReceiverTransaction));
            when(transactionMapper.toTransactionResponse(expectedSenderTransaction)).thenReturn(expectedSenderTransactionResponse);

            // When
            TransactionResponse actualSenderTransactionResponse = underTest.transfer(transactionTransferRequest);

            // Then
            ArgumentCaptor<List<Transaction>> transactionCaptor = ArgumentCaptor.forClass(List.class);
            verify(transactionRepository).saveAll(transactionCaptor.capture());
            List<Transaction> actualListTransaction = transactionCaptor.getValue();
            Transaction actualSenderTransaction = actualListTransaction.get(0);
            Transaction actualReceiverTransaction = actualListTransaction.get(1);

            assertThat(actualSenderTransaction.getCard()).isEqualTo(expectedSenderTransaction.getCard());
            assertThat(actualSenderTransaction.getTransactionDate()).isEqualTo(expectedSenderTransaction.getTransactionDate());
            assertThat(actualSenderTransaction.getType()).isEqualTo(expectedSenderTransaction.getType());
            assertThat(actualSenderTransaction.getAmount()).isEqualByComparingTo(expectedSenderTransaction.getAmount());
            assertThat(actualSenderTransaction.getTargetMaskedCard()).isEqualTo(expectedSenderTransaction.getTargetMaskedCard());
            assertThat(actualSenderTransaction.getDescription()).isEqualTo(expectedSenderTransaction.getDescription());

            assertThat(actualReceiverTransaction.getCard()).isEqualTo(expectedReceiverTransaction.getCard());
            assertThat(actualReceiverTransaction.getTransactionDate()).isEqualTo(expectedReceiverTransaction.getTransactionDate());
            assertThat(actualReceiverTransaction.getType()).isEqualTo(expectedReceiverTransaction.getType());
            assertThat(actualReceiverTransaction.getAmount()).isEqualByComparingTo(expectedReceiverTransaction.getAmount());
            assertThat(actualReceiverTransaction.getTargetMaskedCard()).isEqualTo(expectedReceiverTransaction.getTargetMaskedCard());
            assertThat(actualReceiverTransaction.getDescription()).isEqualTo(expectedReceiverTransaction.getDescription());

            assertThat(actualSenderTransactionResponse.card()).isEqualTo(expectedSenderTransactionResponse.card());
            assertThat(actualSenderTransactionResponse.datetime()).isEqualTo(expectedSenderTransactionResponse.datetime());
            assertThat(actualSenderTransactionResponse.type()).isEqualTo(expectedSenderTransactionResponse.type());
            assertThat(actualSenderTransactionResponse.amount()).isEqualByComparingTo(expectedSenderTransactionResponse.amount());
            assertThat(actualSenderTransactionResponse.targetCard()).isEqualTo(expectedSenderTransactionResponse.targetCard());
            assertThat(actualSenderTransactionResponse.description()).isEqualTo(expectedSenderTransactionResponse.description());

            assertThat(senderCard.getBalance()).isEqualByComparingTo(expectedSenderCardBalance);
            assertThat(receiverCard.getBalance()).isEqualByComparingTo(expectedReceiverCardBalance);

            verify(cardService).findCardByNumber(senderCardNumber, user);
            verify(cardService).findCardByNumber(receiverCardNumber, user);
            verify(cardService).validateCardOwnership(senderCard.getId());
            verify(cardService).validateCardOwnership(receiverCard.getId());
            verify(cardRepository).saveAll(List.of(senderCard, receiverCard));
            verify(transactionMapper).toTransactionResponse(expectedSenderTransaction);
        }
    }

    @Test
    void transfer_whenSenderCardDoesntBelongsToUser_shouldThrowTransactionDeclinedException() {
        try(MockedStatic<SecurityUtil> secureUtil = mockStatic(SecurityUtil.class)) {
            // Given
            Card senderCard = card1;
            Card receiverCard = card2;
            String senderCardNumber = "1111222233334444";
            String receiverCardNumber = "5555666677778888";

            BigDecimal amount = new BigDecimal("150");

            TransactionTransferRequest transactionTransferRequest = new TransactionTransferRequest(
                    senderCardNumber,
                    receiverCardNumber,
                    amount,
                    null
            );

            secureUtil.when(SecurityUtil::getCurrentUser).thenReturn(user);
            when(cardService.findCardByNumber(senderCardNumber, user)).thenReturn(senderCard);
            when(cardService.findCardByNumber(receiverCardNumber, user)).thenReturn(receiverCard);
            when(cardService.validateCardOwnership(senderCard.getId())).thenReturn(false);

            // When
            TransactionDeclinedException exception = assertThrows(
                    TransactionDeclinedException.class,
                    () -> underTest.transfer(transactionTransferRequest)
            );

            // Then
            assertThat(exception).hasMessage("Card does not belong to the user.");

            verify(cardService).findCardByNumber(senderCardNumber, user);
            verify(cardService).findCardByNumber(receiverCardNumber, user);
            verify(cardService).validateCardOwnership(senderCard.getId());
            verifyNoMoreInteractions(cardService);
            verifyNoInteractions(cardRepository);
            verifyNoInteractions(transactionRepository);
            verifyNoInteractions(transactionMapper);
        }
    }

    @Test
    void transfer_whenReceiverCardDoesntBelongsToUser_shouldThrowTransactionDeclinedException() {
        try(MockedStatic<SecurityUtil> secureUtil = mockStatic(SecurityUtil.class)) {
            // Given
            Card senderCard = card1;
            Card receiverCard = card2;
            String senderCardNumber = "1111222233334444";
            String receiverCardNumber = "5555666677778888";

            BigDecimal amount = new BigDecimal("150");

            TransactionTransferRequest transactionTransferRequest = new TransactionTransferRequest(
                    senderCardNumber,
                    receiverCardNumber,
                    amount,
                    null
            );

            secureUtil.when(SecurityUtil::getCurrentUser).thenReturn(user);
            when(cardService.findCardByNumber(senderCardNumber, user)).thenReturn(senderCard);
            when(cardService.findCardByNumber(receiverCardNumber, user)).thenReturn(receiverCard);
            when(cardService.validateCardOwnership(senderCard.getId())).thenReturn(true);
            when(cardService.validateCardOwnership(receiverCard.getId())).thenReturn(false);

            // When
            TransactionDeclinedException exception = assertThrows(
                    TransactionDeclinedException.class,
                    () -> underTest.transfer(transactionTransferRequest)
            );

            // Then
            assertThat(exception).hasMessage("Card does not belong to the user.");

            verify(cardService).findCardByNumber(senderCardNumber, user);
            verify(cardService).findCardByNumber(receiverCardNumber, user);
            verify(cardService).validateCardOwnership(senderCard.getId());
            verify(cardService).validateCardOwnership(receiverCard.getId());
            verifyNoInteractions(cardRepository);
            verifyNoInteractions(transactionRepository);
            verifyNoInteractions(transactionMapper);
        }
    }

    @Test
    void getTransactionsByUserCard_whenCardExistAndBelongsToUser_shouldReturnPageOfTransactionResponse() {
        // Given
        Card usedCard = card1;

        TransactionType type = TransactionType.WRITE_OFF;
        int page = 0;
        int size = 10;
        List<String> sortList = List.of("id", "card.status", "amount");
        Sort sortBy = Sort.by(List.of(
                Sort.Order.desc("id"),
                Sort.Order.desc("card.status"),
                Sort.Order.desc("amount")
        ));
        String sortOrder = "desc";

        Pageable pageable = PageRequest.of(
                page,
                size,
                sortBy
        );

        LocalDateTime from = LocalDateTime.of(2025, 1, 1, 10, 0, 0);
        LocalDateTime to = LocalDateTime.of(2025, 2, 1, 10, 0, 0);
        TransactionParamFilter filter = new TransactionParamFilter(
                usedCard.getId(),
                type,
                from,
                to,
                false
        );

        BigDecimal amount = new BigDecimal("100");
        Transaction transaction1 = new Transaction();
        transaction1.setCard(usedCard);
        transaction1.setType(TransactionType.WRITE_OFF);
//        transaction1.setTransactionDate(transactionDateTime);
        transaction1.setAmount(amount);

//        Transaction transaction2 = new Transaction();
//        transaction2.setCard(usedCard);
//        transaction2.setType(TransactionType.REPLENISHMENT);
//        transaction2.setTransactionDate(transactionDateTime);
//        transaction2.setAmount(new BigDecimal("200"));

        TransactionResponse expectedSenderTransactionResponse = new TransactionResponse(
                amount,
                TransactionType.WRITE_OFF,
                new CardResponse(
                        null,
                        LocalDate.now().plusYears(1),
                        null,
                        card1.getStatus(),
                        card1.getBalance(),
                        List.of(new LimitResponse(limit1.getLimitType(), limit1.getMaxAmount()))),
                null,
                null,
                null
        );

        List<Transaction> transactionsFromDB = List.of(transaction1);

        Page<Transaction> pageFromDB = new PageImpl<>(
                transactionsFromDB,
                pageable,
                transactionsFromDB.size()
        );

        when(cardService.validateCardOwnership(usedCard.getId())).thenReturn(true);
        when(transactionRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(pageFromDB);
        when(transactionMapper.toTransactionResponse(transaction1)).thenReturn(expectedSenderTransactionResponse);


        // When
        Page<TransactionResponse> actualPage = underTest.getTransactionsByUserCard(
                usedCard.getId(), filter, page, size, sortList, sortOrder);

        // Then
        assertThat(actualPage).isNotNull();
        assertThat(actualPage.getContent()).hasSize(1);
        assertThat(actualPage.getContent().get(0)).isEqualTo(expectedSenderTransactionResponse);
        assertThat(actualPage.getPageable().getPageNumber()).isEqualTo(page);
        assertThat(actualPage.getPageable().getPageSize()).isEqualTo(size);
        assertThat(actualPage.getTotalElements()).isEqualTo(transactionsFromDB.size());

        verify(cardService).validateCardOwnership(usedCard.getId());
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(transactionRepository).findAll(any(Specification.class), pageableCaptor.capture());
        Pageable capturedPageable = pageableCaptor.getValue();

        assertThat(capturedPageable.getPageNumber()).isEqualTo(page);
        assertThat(capturedPageable.getPageSize()).isEqualTo(size);
        assertThat(capturedPageable.getSort()).isEqualTo(sortBy);

        verify(transactionMapper).toTransactionResponse(transaction1);
    }

    @Test
    void getTransactionsByUserCard_whenCardDoesntBelongsToUser_shouldThrowCardNotFoundException() {
        // Given
        // When
        // Then
    }

    @Test
    void getTransactionsByCard_whenCardExist_shouldReturnPageOfTransactionResponse() {
        // Given
        // When
        // Then
    }

    @Test
    void getTransactionsByCard_whenCardDoesntExist_shouldThrowCardNotFoundException() {
        // Given
        // When
        // Then
    }
}