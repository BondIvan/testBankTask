package com.testtask.bankcardmanagement.service.card.impl;

import com.testtask.bankcardmanagement.encrypt.AESEncryption;
import com.testtask.bankcardmanagement.exception.card.CardBalanceException;
import com.testtask.bankcardmanagement.exception.card.CardDuplicateException;
import com.testtask.bankcardmanagement.exception.card.CardNotAvailableException;
import com.testtask.bankcardmanagement.exception.card.CardNotFoundException;
import com.testtask.bankcardmanagement.exception.security.AccessDeniedException;
import com.testtask.bankcardmanagement.exception.user.UserNotFoundException;
import com.testtask.bankcardmanagement.model.Card;
import com.testtask.bankcardmanagement.model.Limit;
import com.testtask.bankcardmanagement.model.User;
import com.testtask.bankcardmanagement.model.dto.card.CardParamFilter;
import com.testtask.bankcardmanagement.model.dto.card.CardRequest;
import com.testtask.bankcardmanagement.model.dto.card.CardResponse;
import com.testtask.bankcardmanagement.model.dto.limit.LimitRequest;
import com.testtask.bankcardmanagement.model.dto.limit.LimitResponse;
import com.testtask.bankcardmanagement.model.dto.limit.LimitUpdateRequest;
import com.testtask.bankcardmanagement.model.dto.user.UserResponse;
import com.testtask.bankcardmanagement.model.enums.CardStatus;
import com.testtask.bankcardmanagement.model.enums.LimitType;
import com.testtask.bankcardmanagement.model.enums.UserRole;
import com.testtask.bankcardmanagement.model.mapper.CardMapper;
import com.testtask.bankcardmanagement.model.mapper.LimitMapper;
import com.testtask.bankcardmanagement.repository.CardRepository;
import com.testtask.bankcardmanagement.repository.UserRepository;
import com.testtask.bankcardmanagement.service.security.SecurityUtil;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {
    @Mock private CardRepository cardRepository;
    @Mock private UserRepository userRepository;
    @Mock private AESEncryption aesEncryption;
    @Mock private CardMapper cardMapper;
    @Mock private LimitMapper limitMapper;
    @InjectMocks private CardServiceImpl underTest;

    private final String cardNumber = "1234123412341234";
    private final String userEmail = "example@test.ru";
    private final UserRole userRole = UserRole.USER;

    @Test
    void createCard_whenUserExistAndCardNumberUniqueAndNoLimits_shouldReturnCardResponseDtoWithDefaultLimit() {
        // Given
        CardRequest cardRequest = new CardRequest(
                cardNumber,
                LocalDate.now().plusYears(1),
                userEmail,
                Collections.emptyList()
        );
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail(userEmail);
        existingUser.setRole(userRole);

        String encryptedNumber = "encryptedNumber";

        LimitRequest limitRequest = new LimitRequest(LimitType.NO_LIMIT, null);

        Limit defaultLimit = new Limit();
        defaultLimit.setLimitType(LimitType.NO_LIMIT);
        defaultLimit.setMaxAmount(null);

        Card expectedSavedCard = new Card();
        expectedSavedCard.setUser(existingUser);
        expectedSavedCard.setStatus(CardStatus.ACTIVE);
        expectedSavedCard.setExpirationDate(LocalDate.now().plusYears(1));
        expectedSavedCard.setEncryptedNumber(encryptedNumber);
        expectedSavedCard.setBalance(BigDecimal.ZERO);
        expectedSavedCard.setLimits(List.of(defaultLimit));

        CardResponse expectedCardResponse = new CardResponse(
                "**** **** **** 1234",
                LocalDate.now().plusYears(1),
                new UserResponse(userEmail, "USER"),
                CardStatus.ACTIVE,
                BigDecimal.ZERO,
                List.of(new LimitResponse(LimitType.NO_LIMIT, null))
        );

        when(userRepository.findUserByEmail(cardRequest.ownerEmail())).thenReturn(Optional.of(existingUser));
        when(aesEncryption.encrypt(cardRequest.cardNumber())).thenReturn(encryptedNumber);
        when(cardRepository.findEncryptedNumberByUserId(existingUser.getId())).thenReturn(Collections.emptyList());
        when(limitMapper.toLimit(limitRequest)).thenReturn(defaultLimit);
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cardMapper.toCardResponse(expectedSavedCard)).thenReturn(expectedCardResponse);

        // When
        CardResponse actualCardResponse = underTest.createCard(cardRequest);

        // Then
        ArgumentCaptor<Card> cardCapture = ArgumentCaptor.forClass(Card.class);
        verify(cardRepository, times(2)).save(cardCapture.capture());
        verify(userRepository).findUserByEmail(userEmail);
        verify(aesEncryption).encrypt(cardNumber);
        verify(limitMapper, times(1)).toLimit(limitRequest);

        Card savedCard = cardCapture.getValue();
        assertThat(savedCard).isEqualTo(expectedSavedCard);
        assertThat(actualCardResponse).isEqualTo(expectedCardResponse);

        verify(cardMapper).toCardResponse(savedCard);
    }

    @Test
    void createCard_whenUserExistAndCardNumberUniqueAndLimitsNull_shouldReturnCardResponseDtoWithDefaultLimit() {
        // Given
        CardRequest cardRequest = new CardRequest(
                cardNumber,
                LocalDate.now().plusYears(1),
                userEmail,
                null
        );
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail(userEmail);
        existingUser.setRole(userRole);

        String encryptedNumber = "encryptedNumber";

        LimitRequest limitRequest = new LimitRequest(LimitType.NO_LIMIT, null);

        Limit defaultLimit = new Limit();
        defaultLimit.setLimitType(LimitType.NO_LIMIT);
        defaultLimit.setMaxAmount(null);

        Card expectedSavedCard = new Card();
        expectedSavedCard.setUser(existingUser);
        expectedSavedCard.setStatus(CardStatus.ACTIVE);
        expectedSavedCard.setExpirationDate(LocalDate.now().plusYears(1));
        expectedSavedCard.setEncryptedNumber(encryptedNumber);
        expectedSavedCard.setBalance(BigDecimal.ZERO);
        expectedSavedCard.setLimits(List.of(defaultLimit));

        CardResponse expectedCardResponse = new CardResponse(
                "**** **** **** 1234",
                LocalDate.now().plusYears(1),
                new UserResponse(userEmail, "USER"),
                CardStatus.ACTIVE,
                BigDecimal.ZERO,
                List.of(new LimitResponse(LimitType.NO_LIMIT, null))
        );

        when(userRepository.findUserByEmail(cardRequest.ownerEmail())).thenReturn(Optional.of(existingUser));
        when(aesEncryption.encrypt(cardRequest.cardNumber())).thenReturn(encryptedNumber);
        when(cardRepository.findEncryptedNumberByUserId(existingUser.getId())).thenReturn(Collections.emptyList());
        when(limitMapper.toLimit(limitRequest)).thenReturn(defaultLimit);
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cardMapper.toCardResponse(expectedSavedCard)).thenReturn(expectedCardResponse);

        // When
        CardResponse actualCardResponse = underTest.createCard(cardRequest);

        // Then
        ArgumentCaptor<Card> cardCapture = ArgumentCaptor.forClass(Card.class);
        verify(cardRepository, times(2)).save(cardCapture.capture());
        verify(userRepository).findUserByEmail(userEmail);
        verify(aesEncryption).encrypt(cardNumber);
        verify(limitMapper, times(1)).toLimit(limitRequest);

        Card savedCard = cardCapture.getValue();
        assertThat(savedCard).isEqualTo(expectedSavedCard);
        assertThat(actualCardResponse).isEqualTo(expectedCardResponse);

        verify(cardMapper).toCardResponse(savedCard);
    }

    @Test
    void createCard_whenUserExistAndCardNumberUniqueAndWithLimits_shouldReturnCardResponseDto() {
        // Given
        List<LimitRequest> limitRequests = List.of(
                new LimitRequest(LimitType.DAILY, new BigDecimal("1000")),
                new LimitRequest(LimitType.MONTHLY, new BigDecimal("3000"))
        );
        CardRequest cardRequest = new CardRequest(
                cardNumber,
                LocalDate.now().plusYears(1),
                userEmail,
                limitRequests
        );
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail(userEmail);
        existingUser.setRole(userRole);

        String encryptedNumber = "encryptedNumber";

        List<Limit> limits = limitRequests.stream()
                .map(limitRequest -> {
                    Limit limit = new Limit();
                    limit.setLimitType(limitRequest.type());
                    limit.setMaxAmount(limitRequest.maxAmount());
                    return limit;
                })
                .toList();

        Card expectedSavedCard = new Card();
        expectedSavedCard.setUser(existingUser);
        expectedSavedCard.setStatus(CardStatus.ACTIVE);
        expectedSavedCard.setExpirationDate(LocalDate.now().plusYears(1));
        expectedSavedCard.setEncryptedNumber(encryptedNumber);
        expectedSavedCard.setBalance(BigDecimal.ZERO);
        expectedSavedCard.setLimits(limits);

        CardResponse expectedCardResponse = new CardResponse(
                "**** **** **** 1234",
                LocalDate.now().plusYears(1),
                new UserResponse(userEmail, "USER"),
                CardStatus.ACTIVE,
                BigDecimal.ZERO,
                List.of(
                        new LimitResponse(LimitType.DAILY, new BigDecimal("1000")),
                        new LimitResponse(LimitType.MONTHLY, new BigDecimal("5000"))
                )
        );

        when(userRepository.findUserByEmail(cardRequest.ownerEmail())).thenReturn(Optional.of(existingUser));
        when(aesEncryption.encrypt(cardRequest.cardNumber())).thenReturn(encryptedNumber);
        when(cardRepository.findEncryptedNumberByUserId(existingUser.getId())).thenReturn(Collections.emptyList());
        when(limitMapper.toLimit(limitRequests.get(0))).thenReturn(limits.get(0));
        when(limitMapper.toLimit(limitRequests.get(1))).thenReturn(limits.get(1));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cardMapper.toCardResponse(expectedSavedCard)).thenReturn(expectedCardResponse);

        // When
        CardResponse actualCardResponse = underTest.createCard(cardRequest);

        // Then
        ArgumentCaptor<Card> cardCapture = ArgumentCaptor.forClass(Card.class);
        verify(cardRepository, times(2)).save(cardCapture.capture());
        verify(userRepository).findUserByEmail(userEmail);
        verify(aesEncryption).encrypt(cardNumber);
        verify(limitMapper).toLimit(limitRequests.get(0));
        verify(limitMapper).toLimit(limitRequests.get(1));

        Card savedCard = cardCapture.getValue();
        assertThat(savedCard).isEqualTo(expectedSavedCard);
        assertThat(actualCardResponse).isEqualTo(expectedCardResponse);

        verify(cardMapper).toCardResponse(savedCard);
    }

    @Test
    void createCard_whenUserExistAndCardNumberDuplicate_shouldThrowCardDuplicateException() {
        // Given
        CardRequest cardRequest = new CardRequest(
                cardNumber,
                LocalDate.now().plusYears(1),
                userEmail,
                Collections.emptyList()
        );

        User existingUser = new User();
        existingUser.setId(1L);

        String existEncryptedNumber = "encryptedNumber";

        when(userRepository.findUserByEmail(cardRequest.ownerEmail())).thenReturn(Optional.of(existingUser));
        when(cardRepository.findEncryptedNumberByUserId(existingUser.getId())).thenReturn(List.of(existEncryptedNumber));
        when(aesEncryption.decrypt(existEncryptedNumber)).thenReturn(cardRequest.cardNumber());

        // When
        CardDuplicateException exception = assertThrows(
                CardDuplicateException.class,
                () -> underTest.createCard(cardRequest)
        );

        // Then
        verify(cardRepository).findEncryptedNumberByUserId(existingUser.getId());
        verify(aesEncryption).decrypt(existEncryptedNumber);
        verifyNoInteractions(cardMapper);
        verifyNoInteractions(limitMapper);
        verifyNoMoreInteractions(cardRepository);

        assertThat(exception).hasMessage("A card with this number already exists.");
    }

    @Test
    void createCard_whenUserDoesntExist_shouldThrowUserNotFoundException() {
        // Given
        CardRequest cardRequest = new CardRequest(
                cardNumber,
                LocalDate.now().plusYears(1),
                userEmail,
                Collections.emptyList()
        );

        when(userRepository.findUserByEmail(cardRequest.ownerEmail())).thenReturn(Optional.empty());

        // When
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> underTest.createCard(cardRequest)
        );

        // Then
        verify(userRepository).findUserByEmail(cardRequest.ownerEmail());
        verifyNoInteractions(aesEncryption);
        verifyNoInteractions(cardRepository);
        verifyNoInteractions(cardMapper);
        verifyNoInteractions(limitMapper);

        assertThat(exception).hasMessage("User with such email not found.");
    }

    @Test
    void getAllCards_shouldReturnPageOfCardResponse() {
        // Given
        CardParamFilter filter = new CardParamFilter(
                CardStatus.ACTIVE,
                "user@mail.com"
        );

        List<String> sortList = List.of("user.email", "status");
        String sortOrder = "desc";
        int page = 0;
        int size = 10;

        Pageable expectedPageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Order.desc("user.email"),
                        Sort.Order.desc("status"))
        );

        Card card = new Card();
        CardResponse mockResponse = mock(CardResponse.class);

        when(cardRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(card)));
        when(cardMapper.toCardResponse(card)).thenReturn(mockResponse);

        // When
        Page<CardResponse> resultPage = underTest.getAllCards(
                filter,
                page,
                size,
                sortList,
                sortOrder
        );

        // Then
        ArgumentCaptor<Specification<Card>> specCardCaptor = ArgumentCaptor.forClass(Specification.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(cardRepository).findAll(specCardCaptor.capture(), pageableCaptor.capture());

        Pageable actualPageable = pageableCaptor.getValue();
        assertThat(actualPageable.getPageNumber()).isEqualTo(page);
        assertThat(actualPageable.getPageSize()).isEqualTo(size);
        assertThat(actualPageable.getSort().getOrderFor("user.email"))
                .isNotNull()
                .extracting(Sort.Order::getDirection)
                .isEqualTo(Sort.Direction.DESC);
        assertThat(actualPageable).isEqualTo(expectedPageable);

        verify(cardMapper).toCardResponse(card);

        assertThat(resultPage.getContent()).hasSize(1);
        assertThat(resultPage.getContent()).containsExactlyInAnyOrder(mockResponse);
        assertThat(resultPage.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getAllCardsForCurrentUser_shouldPageOfCardResponse() {
        try (MockedStatic<SecurityUtil> utilities = mockStatic(SecurityUtil.class)) {
            // Given
            User user = new User();
            user.setEmail("user@example.com");

            CardParamFilter inputFilter = new CardParamFilter(
                    CardStatus.ACTIVE,
                    null
            );

            int page = 0;
            int size = 10;
            Pageable expectedPageable = PageRequest.of(page, size);

            Card card1 = new Card();
            card1.setEncryptedNumber("encrypted number 1");
            Card card2 = new Card();
            card2.setEncryptedNumber("encrypted number 2");
            CardResponse response1 = mock(CardResponse.class);
            CardResponse response2 = mock(CardResponse.class);

            utilities.when(SecurityUtil::getCurrentUser).thenReturn(user);
            when(cardRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(card1, card2)));
            when(cardMapper.toCardResponse(card1)).thenReturn(response1);
            when(cardMapper.toCardResponse(card2)).thenReturn(response2);

            // When
            Page<CardResponse> resultPage = underTest.getAllCardsForCurrentUser(
                    inputFilter,
                    page,
                    size
            );

            // Then
            ArgumentCaptor<Specification<Card>> specCaptor = ArgumentCaptor.forClass(Specification.class);
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

            verify(cardRepository).findAll(specCaptor.capture(), pageableCaptor.capture());

            Pageable actualPageable = pageableCaptor.getValue();
            assertThat(actualPageable.getPageNumber()).isEqualTo(page);
            assertThat(actualPageable.getPageSize()).isEqualTo(size);
            assertThat(actualPageable).isEqualTo(expectedPageable);

            verify(cardMapper, times(2)).toCardResponse(any(Card.class));

            assertThat(resultPage.getContent()).hasSize(2);
            assertThat(resultPage.getContent()).containsExactlyInAnyOrder(response1, response2);
            assertThat(resultPage.getTotalElements()).isEqualTo(2);
            assertThat(resultPage.getPageable()).isEqualTo(expectedPageable);
        }
    }

    @Test
    void blockCard_whenCardExist_shouldChangeCardStatusToBlock() {
        // Given
        Long id = 1L;

        Card activeCard = new Card();
        activeCard.setStatus(CardStatus.ACTIVE);

        CardResponse expectedCardResponse = new CardResponse(
                null,
                null,
                null,
                CardStatus.BLOCKED,
                null,
                null
        );

        when(cardRepository.findById(id)).thenReturn(Optional.of(activeCard));
        when(cardRepository.save(any(Card.class))).thenReturn(activeCard);
        when(cardMapper.toCardResponse(activeCard)).thenReturn(expectedCardResponse);

        // When
        CardResponse actualCardResponse = underTest.blockCard(id);

        // Then
        ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);
        verify(cardRepository).save(cardCaptor.capture());
        Card actualCard = cardCaptor.getValue();

        assertThat(actualCard.getStatus()).isEqualTo(CardStatus.BLOCKED);
        assertThat(expectedCardResponse.status()).isEqualTo(CardStatus.BLOCKED);
        assertThat(actualCardResponse).isEqualTo(expectedCardResponse);

        verify(cardRepository).findById(id);
        verify(cardRepository, times(1)).save(activeCard);
        verify(cardMapper).toCardResponse(activeCard);
    }

    @Test
    void blockCard_whenCardDoesntExist_shouldThrowCardNotFoundException() {
        // Given
        Long id = 1L;

        when(cardRepository.findById(id)).thenReturn(Optional.empty());

        // When
        CardNotFoundException exception = assertThrows(
                CardNotFoundException.class,
                () -> underTest.blockCard(id)
        );

        // Then
        verify(cardRepository).findById(id);
        verifyNoMoreInteractions(cardRepository);
        verifyNoInteractions(cardMapper);

        assertThat(exception).hasMessage("A card with such id not found.");
    }

    @Test
    void activateCard_whenCardExist_shouldChangeCardStatusToActive() {
        // Given
        Long id = 1L;

        Card blockedCard = new Card();
        blockedCard.setStatus(CardStatus.BLOCKED);

        CardResponse expectedCardResponse = new CardResponse(
                null,
                null,
                null,
                CardStatus.ACTIVE,
                null,
                null
        );

        when(cardRepository.findById(id)).thenReturn(Optional.of(blockedCard));
        when(cardRepository.save(any(Card.class))).thenReturn(blockedCard);
        when(cardMapper.toCardResponse(blockedCard)).thenReturn(expectedCardResponse);

        // When
        CardResponse actualCardResponse = underTest.activateCard(id);

        // Then
        ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);
        verify(cardRepository).save(cardCaptor.capture());

        Card actualCard = cardCaptor.getValue();

        assertThat(actualCard.getStatus()).isEqualTo(CardStatus.ACTIVE);
        assertThat(expectedCardResponse.status()).isEqualTo(CardStatus.ACTIVE);
        assertThat(actualCardResponse).isEqualTo(expectedCardResponse);

        verify(cardRepository).findById(id);
        verify(cardRepository, times(1)).save(blockedCard);
        verify(cardMapper).toCardResponse(blockedCard);
    }

    @Test
    void activateCard_whenCardDoesntExist_shouldThrowCardNotFoundException() {
        // Given
        Long id = 1L;

        when(cardRepository.findById(id)).thenReturn(Optional.empty());

        // When
        CardNotFoundException exception = assertThrows(
                CardNotFoundException.class,
                () -> underTest.activateCard(id)
        );

        // Then
        verify(cardRepository).findById(id);
        verifyNoMoreInteractions(cardRepository);
        verifyNoInteractions(cardMapper);

        assertThat(exception).hasMessage("A card with such id not found.");
    }

    @Test
    void deleteCard_whenBalanceValid_shouldDeleteCard() {
        // Given
        Long id = 1L;
        Card card = new Card();
        card.setBalance(BigDecimal.ZERO);

        when(cardRepository.findById(id)).thenReturn(Optional.of(card));

        // When
        underTest.deleteCard(id);

        // Then
        verify(cardRepository).findById(id);
        verify(cardRepository).delete(card);
    }

    @Test
    void deleteCard_whenCardNotFound_shouldThrowCardNotFoundException() {
        // Given
        Long id = 1L;

        when(cardRepository.findById(id)).thenReturn(Optional.empty());

        // When
        CardNotFoundException exception = assertThrows(
                CardNotFoundException.class,
                () -> underTest.deleteCard(id)
        );

        // Then
        verify(cardRepository).findById(id);
        verifyNoMoreInteractions(cardRepository);

        assertThat(exception).hasMessage("A card with such id not found.");
    }

    @Test
    void deleteCard_whenCardBalanceNotZero_shouldThrowCardBalanceException() {
        // Given
        Long id = 1L;
        Card card = new Card();
        card.setBalance(new BigDecimal("100")); // not zero balance

        when(cardRepository.findById(id)).thenReturn(Optional.of(card));

        // When
        CardBalanceException exception = assertThrows(
                CardBalanceException.class,
                () -> underTest.deleteCard(id)
        );

        // Then
        verify(cardRepository).findById(id);
        verifyNoMoreInteractions(cardRepository);

        assertThat(exception).hasMessage("Cannot delete a card with a non-zero amount.");
    }

    @Test
    void validateCardOwnership_whenUserOwnerTheCard_shouldReturnTrue() {
        try(MockedStatic<SecurityUtil> secureUtil = mockStatic(SecurityUtil.class)) {
            // Given
            Long id = 1L;
            User ownerUser = new User();
            ownerUser.setId(1L);

            secureUtil.when(SecurityUtil::getCurrentUser).thenReturn(ownerUser);
            when(cardRepository.existsByIdAndUserId(id, ownerUser.getId())).thenReturn(true);

            // When
            boolean actualOwnership = underTest.validateCardOwnership(id);

            // Then
            verify(cardRepository).existsByIdAndUserId(id, ownerUser.getId());
            assertTrue(actualOwnership);
        }
    }

    @Test
    void validateCardOwnership_whenUserNotOwnerTheCard_shouldThrowAccessDeniedException() {
        try(MockedStatic<SecurityUtil> secureUtil = mockStatic(SecurityUtil.class)) {
            // Given
            Long id = 1L;
            User notOwnerUser = new User();
            notOwnerUser.setId(1L);

            secureUtil.when(SecurityUtil::getCurrentUser).thenReturn(notOwnerUser);
            when(cardRepository.existsByIdAndUserId(id, notOwnerUser.getId())).thenReturn(false);

            // When
            AccessDeniedException exception = assertThrows(
                    AccessDeniedException.class,
                    () -> underTest.validateCardOwnership(id)
            );

            // Then
            verify(cardRepository).existsByIdAndUserId(id, notOwnerUser.getId());
            assertThat(exception).hasMessage("Card does not belong to the user.");
        }
    }

    @Test
    void isCardAvailable_whenCardIsActive_shouldReturnTrue() {
        // Given
        Card validCard = new Card();
        validCard.setStatus(CardStatus.ACTIVE);
        validCard.setExpirationDate(LocalDate.now().plusYears(1));

        // When
        boolean actualValid = underTest.isCardAvailable(validCard);

        // Then
        assertTrue(actualValid);
    }

    @Test
    void isCardAvailable_whenCardIsBlocked_shouldThrowCardNotAvailableException() {
        // Given
        Card blockedCard = new Card();
        blockedCard.setStatus(CardStatus.BLOCKED);

        // When
        CardNotAvailableException exception = assertThrows(
                CardNotAvailableException.class,
                () -> underTest.isCardAvailable(blockedCard)
        );

        // Then
        assertThat(exception).hasMessage("This card is blocked");
    }

    @Test
    void isCardAvailable_whenCardIsExpired_shouldThrowCardNotAvailableException() {
        // Given
        Card expiredCard = new Card();
        expiredCard.setStatus(CardStatus.EXPIRED);
        expiredCard.setExpirationDate(LocalDate.now().minusYears(1));

        // When
        CardNotAvailableException exception = assertThrows(
                CardNotAvailableException.class,
                () -> underTest.isCardAvailable(expiredCard)
        );

        // Then
        assertThat(exception).hasMessage("This card id expired. The expiration date has expired at "
                + expiredCard.getExpirationDate());
    }

    @Test
    void updateCardLimit_whenCardFound_shouldReturnCardResponseDto() {
        // Given
        Long id = 1L;
        Card card = new Card();
        card.setId(id);
        card.setLimits(new ArrayList<>(List.of(
                new Limit(null, card, LimitType.NO_LIMIT, null)
        )));

        LimitUpdateRequest limitUpdateRequest = new LimitUpdateRequest(new ArrayList<>(List.of(
                new LimitRequest(LimitType.DAILY, new BigDecimal("1000")),
                new LimitRequest(LimitType.MONTHLY, new BigDecimal("3000"))
        )));

        Limit limitDay = new Limit();
        limitDay.setLimitType(LimitType.DAILY);
        limitDay.setMaxAmount(new BigDecimal("1000"));

        Limit limitMonth = new Limit();
        limitMonth.setLimitType(LimitType.MONTHLY);
        limitMonth.setMaxAmount(new BigDecimal("3000"));

        CardResponse expectedCardResponse = new CardResponse(
                null,
                null,
                null,
                null,
                null,
                List.of(
                        new LimitResponse(LimitType.DAILY, new BigDecimal("1000")),
                        new LimitResponse(LimitType.MONTHLY, new BigDecimal("3000"))
                )
        );

        when(cardRepository.findById(id)).thenReturn(Optional.of(card));
        when(limitMapper.toLimit(limitUpdateRequest.limits().get(0))).thenReturn(limitDay);
        when(limitMapper.toLimit(limitUpdateRequest.limits().get(1))).thenReturn(limitMonth);
        when(cardRepository.save(any(Card.class))).thenReturn(card);
        when(cardMapper.toCardResponse(card)).thenReturn(expectedCardResponse);

        // When
        CardResponse actualCardResponse = underTest.updateCardLimit(card.getId(), limitUpdateRequest);

        // Then
        ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);
        verify(cardRepository).save(cardCaptor.capture());

        verify(limitMapper).toLimit(limitUpdateRequest.limits().get(0));
        verify(limitMapper).toLimit(limitUpdateRequest.limits().get(1));
        verify(cardRepository).findById(id);
        verify(cardMapper).toCardResponse(card);

        Card savedCard = cardCaptor.getValue();

        assertThat(savedCard.getLimits()).hasSize(2);
        assertThat(savedCard.getLimits()).containsExactlyInAnyOrder(limitDay, limitMonth);
        assertThat(actualCardResponse).isEqualTo(expectedCardResponse);
    }

    @Test
    void updateCardLimit_whenCardNotFound_shouldThrowCardNotFoundException() {
        // Given
        Long id = 1L;

        when(cardRepository.findById(id)).thenReturn(Optional.empty());

        // When
        CardNotFoundException exception = assertThrows(
                CardNotFoundException.class,
                () -> underTest.updateCardLimit(id, any(LimitUpdateRequest.class))
        );

        // Then
        verify(cardRepository).findById(id);
        verifyNoMoreInteractions(cardRepository);
        verifyNoInteractions(limitMapper);
        verifyNoInteractions(cardMapper);

        assertThat(exception).hasMessage("The card with such id not found.");
    }

    @Test
    void existById_whenCardExist_shouldReturnTrue() {
        // Given
        Long id = 1L;

        when(cardRepository.existsById(id)).thenReturn(true);

        // When
        boolean actualExist = underTest.existById(id);

        // Then
        verify(cardRepository).existsById(id);
        assertTrue(actualExist);
    }

    @Test
    void existById_whenCardDoesntExist_shouldReturnFalse() {
        // Given
        Long id = 1L;

        when(cardRepository.existsById(id)).thenReturn(false);

        // When
        boolean actualExist = underTest.existById(id);

        // Then
        verify(cardRepository).existsById(id);
        assertFalse(actualExist);
    }

    @Test
    void findCardByNumber_whenCardExist_shouldReturnCard() {
        // Given
        User owner = new User();
        String searchedCardNumber = "searchedCardNumber"; // card2
        String notSearchedCardNumber = "notSearchedCardNumber";

        Card card1 = new Card();
        card1.setId(1L);
        card1.setEncryptedNumber("encrypted_card1_number");
        card1.setUser(owner);
        Card card2 = new Card();
        card2.setId(2L);
        card2.setEncryptedNumber("encrypted_card2_number");
        card2.setUser(owner);

        owner.setCards(List.of(card1, card2));

        when(cardRepository.findAllByUser(owner)).thenReturn(List.of(card1, card2));
        when(aesEncryption.decrypt(card1.getEncryptedNumber())).thenReturn(notSearchedCardNumber);
        when(aesEncryption.decrypt(card2.getEncryptedNumber())).thenReturn(searchedCardNumber);

        // When
        Card actualCard = underTest.findCardByNumber(searchedCardNumber, owner);

        // Then
        assertThat(actualCard.getUser()).isEqualTo(owner);
        assertThat(actualCard.getEncryptedNumber()).isEqualTo(card2.getEncryptedNumber());

        verify(cardRepository).findAllByUser(owner);
        verify(aesEncryption).decrypt(card1.getEncryptedNumber());
        verify(aesEncryption).decrypt(card2.getEncryptedNumber());
    }

    @Test
    void findCardByNumber_whenCardDoesntExist_shouldThrowCardNotFoundException() {
        // Given
        User owner = new User();
        String searchedCardNumber = "searchedCardNumber"; // card2
        String notSearchedCardNumber = "notSearchedCardNumber";

        Card card1 = new Card();
        card1.setId(1L);
        card1.setEncryptedNumber("encrypted_card1_number");
        card1.setUser(owner);
        Card card2 = new Card();
        card2.setId(2L);
        card2.setEncryptedNumber("encrypted_card2_number");
        card2.setUser(owner);

        when(cardRepository.findAllByUser(owner)).thenReturn(List.of(card1, card2));
        when(aesEncryption.decrypt(card1.getEncryptedNumber())).thenReturn(notSearchedCardNumber);
        when(aesEncryption.decrypt(card2.getEncryptedNumber())).thenReturn(notSearchedCardNumber);

        // When
        CardNotFoundException exception = assertThrows(
                CardNotFoundException.class,
                () -> underTest.findCardByNumber(searchedCardNumber, owner)
        );

        // Then
        assertThat(exception).hasMessage("You don't have a card with that number - " + searchedCardNumber + ".");

        verify(cardRepository).findAllByUser(owner);
        verify(aesEncryption).decrypt(card1.getEncryptedNumber());
        verify(aesEncryption).decrypt(card2.getEncryptedNumber());
    }
}