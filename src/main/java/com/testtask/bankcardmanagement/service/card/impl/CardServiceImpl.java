package com.testtask.bankcardmanagement.service.card.impl;

import com.testtask.bankcardmanagement.encrypt.AESEncryption;
import com.testtask.bankcardmanagement.exception.card.CardBalanceException;
import com.testtask.bankcardmanagement.exception.card.CardDuplicateException;
import com.testtask.bankcardmanagement.exception.card.CardNotAvailableException;
import com.testtask.bankcardmanagement.exception.card.CardNotFoundException;
import com.testtask.bankcardmanagement.exception.db.SomeDBException;
import com.testtask.bankcardmanagement.exception.security.AccessDeniedException;
import com.testtask.bankcardmanagement.exception.user.UserNotFoundException;
import com.testtask.bankcardmanagement.model.Card;
import com.testtask.bankcardmanagement.model.Limit;
import com.testtask.bankcardmanagement.model.User;
import com.testtask.bankcardmanagement.model.dto.card.CardParamFilter;
import com.testtask.bankcardmanagement.model.dto.card.CardRequest;
import com.testtask.bankcardmanagement.model.dto.card.CardResponse;
import com.testtask.bankcardmanagement.model.dto.limit.LimitRequest;
import com.testtask.bankcardmanagement.model.dto.limit.LimitUpdateRequest;
import com.testtask.bankcardmanagement.model.enums.CardStatus;
import com.testtask.bankcardmanagement.model.enums.LimitType;
import com.testtask.bankcardmanagement.model.mapper.CardMapper;
import com.testtask.bankcardmanagement.model.mapper.LimitMapper;
import com.testtask.bankcardmanagement.repository.CardRepository;
import com.testtask.bankcardmanagement.repository.UserRepository;
import com.testtask.bankcardmanagement.service.card.CardService;
import com.testtask.bankcardmanagement.service.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;
    private final AESEncryption aesEncryption;
    private final UserRepository userRepository;
    private final CardMapper cardMapper;
    private final LimitMapper limitMapper;

    @Override
    public CardResponse createCard(CardRequest cardRequest) {
        Optional<User> optionalUser = userRepository.findUserByEmail(cardRequest.ownerEmail());
        if(optionalUser.isEmpty())
            throw new UserNotFoundException("User with such email not found.");

        User owner = optionalUser.get();

        if(isCardNumberDuplicate(owner, cardRequest.cardNumber()))
            throw new CardDuplicateException("A card with this number already exists.");

        List<LimitRequest> limitRequestList = cardRequest.limits();
        if(limitRequestList == null || limitRequestList.isEmpty())
            limitRequestList = List.of(new LimitRequest(LimitType.NO_LIMIT, null));

        Card card = new Card();
        card.setUser(owner);
        card.setEncryptedNumber(aesEncryption.encrypt(cardRequest.cardNumber()));
        card.setExpirationDate(cardRequest.expirationDate());
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.ZERO);

        try {
            Card savedCard = cardRepository.save(card);

            List<Limit> limits = limitRequestList.stream()
                    .map(limitRequest -> {
                        Limit limit = limitMapper.toLimit(limitRequest);
                        limit.setCard(savedCard);
                        return limit;
                    })
                    .collect(Collectors.toList());

            savedCard.setLimits(limits);
            Card savedWithLimits = cardRepository.save(savedCard);
            return cardMapper.toCardResponse(savedWithLimits);
        } catch (DataIntegrityViolationException e) {
            throw new SomeDBException("A card with this number already exists in the database. " + e.getMessage(), e);
        }
    }

    @Override
    public Page<CardResponse> getAllCards(CardParamFilter cardParamFilter, int page, int size, List<String> sortList, String sortOrder) {
        List<Sort.Order> sortOrderList = createSortOrder(sortList, sortOrder);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortOrderList));
        Specification<Card> cardSpec = CardSpecification.build(cardParamFilter);

        List<CardResponse> foundCards = cardRepository.findAll(cardSpec, pageable).stream()
                .map(cardMapper::toCardResponse)
                .toList();

        return new PageImpl<>(
                foundCards,
                pageable,
                foundCards.size());
    }

    @Override
    public Page<CardResponse> getAllCardsForCurrentUser(CardParamFilter cardParamFilter, int page, int size) {
        User user = SecurityUtil.getCurrentUser();
        CardParamFilter userFilter = new CardParamFilter(
                cardParamFilter.status(),
                user.getEmail()
        );
        // Can add new filter to cardParamFilter
        Pageable pageable = PageRequest.of(page, size);
        Specification<Card> cardSpec = CardSpecification.build(userFilter);

        List<CardResponse> foundCards = cardRepository.findAll(cardSpec, pageable).stream()
                .map(cardMapper::toCardResponse)
                .toList();

        return new PageImpl<>(
                foundCards,
                pageable,
                foundCards.size()
        );
    }

    @Override
    public CardResponse blockCard(Long id) {
        Optional<Card> optionalCard = cardRepository.findById(id);
        if(optionalCard.isEmpty())
            throw new CardNotFoundException("A card with such id not found.");

        Card card = optionalCard.get();
        card.setStatus(CardStatus.BLOCKED);

        try {
            Card updatedCard = cardRepository.save(card);
            return cardMapper.toCardResponse(updatedCard);
        } catch (DataIntegrityViolationException e) {
            throw new SomeDBException("DB Error updating card after status change. " + e.getMessage(), e);
        }
    }

    @Override
    public CardResponse activateCard(Long id) {
        Optional<Card> optionalCard = cardRepository.findById(id);
        if(optionalCard.isEmpty())
            throw new CardNotFoundException("A card with such id not found.");

        Card card = optionalCard.get();
        card.setStatus(CardStatus.ACTIVE);

        try {
            Card updatedCard = cardRepository.save(card);
            return cardMapper.toCardResponse(updatedCard);
        } catch (DataIntegrityViolationException e) {
            throw new SomeDBException("DB Error updating card after status change. " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteCard(Long id) {
        Optional<Card> optionalCard = cardRepository.findById(id);
        if(optionalCard.isEmpty())
            throw new CardNotFoundException("A card with such id not found.");

        Card card = optionalCard.get();
        boolean isCardBalanceZero = card.getBalance().signum() == 0;

        if(!isCardBalanceZero)
            throw new CardBalanceException("Cannot delete a card with a non-zero amount.");

        try {
            cardRepository.delete(card);
        } catch (DataIntegrityViolationException e) {
            throw new SomeDBException("DB Error deleting card. " + e.getMessage(), e);
        }
    }

    @Override
    public boolean validateCardOwnership(Long id) {
        User user = SecurityUtil.getCurrentUser();
        boolean isCardOwner = cardRepository.existsByIdAndUserId(id, user.getId());
        if(!isCardOwner)
            throw new AccessDeniedException("Card does not belong to the user.");

        return true;
    }

    @Override
    public boolean isCardAvailable(Card card) {
        if(card.getStatus() == CardStatus.BLOCKED)
            throw new CardNotAvailableException("This card is blocked");

        if(card.getStatus() == CardStatus.EXPIRED)
            throw new CardNotAvailableException("This card id expired. The expiration date has expired at " + card.getExpirationDate());

        return true;
    }

    @Override
    public CardResponse updateCardLimit(Long cardId, LimitUpdateRequest limitUpdateRequest) {
        Optional<Card> optionalCard = cardRepository.findById(cardId);

        if(optionalCard.isEmpty())
            throw new CardNotFoundException("The card with such id not found.");

        Card card = optionalCard.get();

        List<Limit> newLimits = limitUpdateRequest.limits().stream()
                .map(limitRequest -> {
                    Limit limit = limitMapper.toLimit(limitRequest);
                    limit.setCard(card);
                    return limit;
                })
                .collect(Collectors.toList());

        List<Limit> oldLimit = card.getLimits();
        oldLimit.clear();
        oldLimit.addAll(newLimits);

        card.setLimits(oldLimit);
        Card savedCard = cardRepository.save(card);
        return cardMapper.toCardResponse(savedCard);
    }

    @Override
    public boolean existById(@NonNull Long cardId) {
        return cardRepository.existsById(cardId);
    }

    private List<Sort.Order> createSortOrder(List<String> sortList, String sortOrder) {
        Sort.Direction sortDirection = Sort.Direction.fromString(sortOrder);
        return sortList.stream()
                .map(field -> new Sort.Order(sortDirection, field))
                .toList();
    }

    private boolean isCardNumberDuplicate(User user, String cardNumber) {
        List<String> userCards = cardRepository.findEncryptedNumberByUserId(user.getId());
        return userCards.stream()
                .map(aesEncryption::decrypt)
                .anyMatch(number -> number.equals(cardNumber));
    }
}
