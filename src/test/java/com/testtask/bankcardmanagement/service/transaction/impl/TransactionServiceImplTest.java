package com.testtask.bankcardmanagement.service.transaction.impl;

import com.testtask.bankcardmanagement.encrypt.AESEncryption;
import com.testtask.bankcardmanagement.exception.transaction.TransactionDeclinedException;
import com.testtask.bankcardmanagement.model.Card;
import com.testtask.bankcardmanagement.model.Limit;
import com.testtask.bankcardmanagement.model.Transaction;
import com.testtask.bankcardmanagement.model.User;
import com.testtask.bankcardmanagement.model.dto.card.CardResponse;
import com.testtask.bankcardmanagement.model.dto.limit.LimitResponse;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionResponse;
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
    @Mock private AESEncryption aesEncryption;
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
            String notSearchedCardNumber = "notSearchedCardNumber";
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

            when(cardRepository.findAllByUser(user)).thenReturn(List.of(card1, card2));
            when(cardService.validateCardOwnership(card2.getId())).thenReturn(true);
            when(cardService.isCardAvailable(card2)).thenReturn(true);
            doNothing().when(limitService).checkCardLimits(card2, amount);
            when(aesEncryption.decrypt(card1.getEncryptedNumber())).thenReturn(notSearchedCardNumber);
            when(aesEncryption.decrypt(card2.getEncryptedNumber())).thenReturn(searchedCardNumber);
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

            verify(cardRepository).findAllByUser(user);
            verify(aesEncryption).decrypt(card1.getEncryptedNumber());
            verify(aesEncryption).decrypt(card2.getEncryptedNumber());
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
            when(cardRepository.findAllByUser(user)).thenReturn(List.of(card1, card2));
            when(aesEncryption.decrypt(card1.getEncryptedNumber())).thenReturn(searchedCardNumber);
            when(cardService.validateCardOwnership(card1.getId())).thenReturn(true);
            when(cardService.isCardAvailable(card1)).thenReturn(false);

            // When
            TransactionDeclinedException exception = assertThrows(
                    TransactionDeclinedException.class,
                    () -> underTest.writeOff(transactionWriteOffRequest)
            );

            // Then
            assertThat(exception).hasMessage("The card is not valid.");

            verify(cardRepository).findAllByUser(user);
            verify(aesEncryption).decrypt(card1.getEncryptedNumber());
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
            when(cardRepository.findAllByUser(user)).thenReturn(List.of(card1, card2));
            when(aesEncryption.decrypt(card1.getEncryptedNumber())).thenReturn(searchedCardNumber);
            when(cardService.validateCardOwnership(card1.getId())).thenReturn(false);

            // When
            TransactionDeclinedException exception = assertThrows(
                    TransactionDeclinedException.class,
                    () -> underTest.writeOff(transactionWriteOffRequest)
            );

            // Then
            assertThat(exception).hasMessage("Card does not belong to the user.");

            verify(cardRepository).findAllByUser(user);
            verify(aesEncryption).decrypt(card1.getEncryptedNumber());
            verify(cardService).validateCardOwnership(card1.getId());
            verifyNoMoreInteractions(cardRepository);
            verifyNoMoreInteractions(cardService);
            verifyNoInteractions(transactionRepository);
            verifyNoInteractions(transactionMapper);
        }
    }

    @Test
    void writeOff_whenCardNotFound_shouldThrowCardNotFoundException() {

    }

    @Test
    void transfer() {
    }

    @Test
    void getTransactionsByUserCard() {
    }

    @Test
    void getTransactionsByCard() {
    }
}