package com.testtask.bankcardmanagement.service.card.impl;

import com.testtask.bankcardmanagement.encrypt.AESEncryption;
import com.testtask.bankcardmanagement.encrypt.hash.HashCardNumber;
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
import com.testtask.bankcardmanagement.model.dto.card.CardResponse;
import com.testtask.bankcardmanagement.model.dto.card.CreateCardRequest;
import com.testtask.bankcardmanagement.model.dto.limit.LimitUpdateRequest;
import com.testtask.bankcardmanagement.model.enums.CardStatus;
import com.testtask.bankcardmanagement.model.enums.LimitType;
import com.testtask.bankcardmanagement.model.mapper.CardMapper;
import com.testtask.bankcardmanagement.repository.CardRepository;
import com.testtask.bankcardmanagement.repository.UserRepository;
import com.testtask.bankcardmanagement.service.card.CardService;
import com.testtask.bankcardmanagement.service.limit.LimitService;
import com.testtask.bankcardmanagement.service.security.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;
    private final AESEncryption aesEncryption;
    private final UserRepository userRepository;
    private final CardMapper cardMapper;
    private final HashCardNumber hashCardNumber;
    private final LimitService limitService;
    private final SecurityService securityService;

    @Override
    @Transactional
    public CardResponse createCard(CreateCardRequest request) {
        Optional<User> optionalOwner = userRepository.findUserByEmail(request.ownerEmail());
        if(optionalOwner.isEmpty())
            throw new UserNotFoundException("User with such email not found.");

        User owner = optionalOwner.get();

        String hashForNewCard = hashCardNumber.hash(request.cardNumber());
        if(doesUserHaveCardWithThisNumber(owner.getId(), hashForNewCard))
            throw new CardDuplicateException("A card with this number already exists.");

        Card card = new Card();
        card.setUser(owner);
        card.setExpirationDate(request.expirationDate());
        card.setStatus(CardStatus.ACTIVE);
        card.setEncryptedNumber(aesEncryption.encrypt(request.cardNumber()));
        card.setCardHash(hashForNewCard);
        card.setBalance(BigDecimal.ZERO);

        Card savedCard = cardRepository.save(card);

        return cardMapper.toCardResponse(savedCard);
    }

    @Override
    public Page<CardResponse> getAllCards(CardParamFilter filter, int page, int size, List<String> sortList, String sortOrder) {
        List<Sort.Order> sortOrderList = createSortOrder(sortList, sortOrder);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortOrderList));
        Specification<Card> cardSpec = CardSpecification.build(filter);

        List<CardResponse> foundCards = cardRepository.findAll(cardSpec, pageable).stream()
                .map(cardMapper::toCardResponse)
                .toList();

        return new PageImpl<>(
                foundCards,
                pageable,
                foundCards.size());
    }

    @Override
    public CardResponse blockCard(Long id) {
        return null;
    }

    @Override
    public CardResponse activateCard(Long id) {
        return null;
    }

    @Override
    @Transactional
    public boolean deleteCardById(Long cardId) {
        Card card = cardRepository.findCardById(cardId)
                        .orElseThrow(() -> new CardNotFoundException("A card with such id not found."));

        if(card.getBalance().signum() != 0)
            throw new CardBalanceException("Cannot delete a card with a non-zero amount.");

        cardRepository.delete(card);

        return true;
    }

    @Override
    public boolean validateCardOwnership(Long id) {
        User user = securityService.getCurrentUser();
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
    @Transactional
    public CardResponse updateCardLimit(Long userId, Long cardId, LimitUpdateRequest limitUpdateRequest) {
        Card cardWithLimits = cardRepository.findCardWithLimitsByUserIdAndCardId(userId, cardId)
                .orElseThrow(() -> new CardNotFoundException("The user does not have a card with such card id."));

        limitService.setCardLimit(cardWithLimits, limitUpdateRequest.type(), limitUpdateRequest.maxAmount());

        return cardMapper.toCardResponse(cardWithLimits);
    }

    @Override
    @Transactional
    public CardResponse removeCardLimit(Long userId, Long cardId, LimitType limitType) {
        Card cardWithLimits = cardRepository.findCardWithLimitsByUserIdAndCardId(userId, cardId)
                .orElseThrow(() -> new CardNotFoundException("Unable to find user card to remove limit"));

        Limit removingLimit = limitService.getCardLimitByLimitType(cardWithLimits, limitType);

        cardWithLimits.getLimits().remove(removingLimit);

        return cardMapper.toCardResponse(cardWithLimits);
    }

    @Override
    public boolean existById(@NonNull Long cardId) {
        return cardRepository.existsById(cardId);
    }

    @Deprecated
    @Override
    public Card findCardByNumber(String cardNumber, User owner) {
        //TODO Temporary solution (rewrite to cardHash)
        return cardRepository.findAllByUser(owner).stream()
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

    private boolean doesUserHaveCardWithThisNumber(Long userId, String hashNewNumber) {
        return cardRepository.existByUserAndHashNumber(userId, hashNewNumber);
    }
}
