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
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is a service for managing the Card entity
 * @see CardService
 * @see Card
 */
@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;
    private final AESEncryption aesEncryption;
    private final UserRepository userRepository;
    private final CardMapper cardMapper;
    private final LimitMapper limitMapper;

    /**
     * The method creates a new card for the specified user
     * @param cardRequest request object for creating a card
     * @return an object {@link CardResponse} containing data about the created card
     * @see CardRequest
     * @see CardResponse
     * @throws UserNotFoundException If the user with the specified email does not exist
     * @throws CardDuplicateException If the user already has a card with the same number
     */
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
    }

    /**
     * The method gets all cards of all users
     * @param cardParamFilter request object containing filter criteria
     * @param page page number
     * @param size page size
     * @param sortList list of fields to sort by
     * @param sortOrder sort direction (ASC - ascending / DESC - descending)
     * @return an object {@link Page<CardResponse>} representing a page of cards
     * @see Page
     * @see CardParamFilter
     * @see CardSpecification
     */
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

    /**
     * The method gets all cards for the authorized (current) user
     * @param cardParamFilter request object containing filter criteria
     * @param page page number
     * @param size page size
     * @return an object {@link Page<CardResponse>} representing a page of cards
     * @see CardResponse
     * @see CardSpecification
     */
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

    /**
     * The method changes any card status to blocked
     * @param id of the card to be blocked
     * @return an object {@link CardResponse} containing data about the blocked card
     * @see CardStatus
     * @throws CardNotFoundException If the card is not found
     */
    @Override
    public CardResponse blockCard(Long id) {
        Optional<Card> optionalCard = cardRepository.findById(id);
        if(optionalCard.isEmpty())
            throw new CardNotFoundException("A card with such id not found.");

        Card card = optionalCard.get();
        card.setStatus(CardStatus.BLOCKED);

        Card updatedCard = cardRepository.save(card);
        return cardMapper.toCardResponse(updatedCard);
    }

    /**
     * The method changes any card status to activated
     * @param id of the card that needs to be activated
     * @return an {@link CardResponse} object containing data about the activated card
     * @see CardStatus
     * @throws CardNotFoundException If the card is not found
     */
    @Override
    public CardResponse activateCard(Long id) {
        Optional<Card> optionalCard = cardRepository.findById(id);
        if(optionalCard.isEmpty())
            throw new CardNotFoundException("A card with such id not found.");

        Card card = optionalCard.get();
        card.setStatus(CardStatus.ACTIVE);

        Card updatedCard = cardRepository.save(card);
        return cardMapper.toCardResponse(updatedCard);
    }

    /**
     * The method deletes the user's card.
     * @param id of the card to be deleted
     * @see CardStatus
     * @throws CardNotFoundException If the card is not found
     * @throws CardBalanceException If the card balance is not zero
     */
    @Override
    public void deleteCard(Long id) {
        Optional<Card> optionalCard = cardRepository.findById(id);
        if(optionalCard.isEmpty())
            throw new CardNotFoundException("A card with such id not found.");

        Card card = optionalCard.get();
        boolean isCardBalanceZero = card.getBalance().signum() == 0;

        if(!isCardBalanceZero)
            throw new CardBalanceException("Cannot delete a card with a non-zero amount.");

        cardRepository.delete(card);
    }

    /**
     * The method checks whether the user is the owner of the card
     * @param id of the card being checked
     * @return {@code  true}, if the card belongs to the user
     * @see SecurityUtil
     * @throws AccessDeniedException If the card does not belong to the user
     */
    @Override
    public boolean validateCardOwnership(Long id) {
        User user = SecurityUtil.getCurrentUser();
        boolean isCardOwner = cardRepository.existsByIdAndUserId(id, user.getId());
        if(!isCardOwner)
            throw new AccessDeniedException("Card does not belong to the user.");

        return true;
    }

    /**
     * The method checks whether the card is active
     * @param card that needs to be checked
     * @return {@code  true}, if the card is active
     * @see CardStatus
     * @throws CardNotAvailableException If the card is blocked or expired
     */
    @Override
    public boolean isCardAvailable(Card card) {
        if(card.getStatus() == CardStatus.BLOCKED)
            throw new CardNotAvailableException("This card is blocked");

        if(card.getStatus() == CardStatus.EXPIRED)
            throw new CardNotAvailableException("This card id expired. The expiration date has expired at " + card.getExpirationDate());

        return true;
    }

    /**
     * The method updates the limits for the card
     * @param cardId id of the card for which you need to update the limits
     * @param limitUpdateRequest an object containing information about new limits
     * @return an {@link CardResponse} object containing data about the updated card
     * @see LimitUpdateRequest
     * @see LimitType
     * @throws CardNotFoundException If the card is not found
     */
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

    /**
     * The method checks if the card exists
     * @param cardId id of the card being checked
     * @return {@code  true}, if the card exists
     */
    @Override
    public boolean existById(@NonNull Long cardId) {
        return cardRepository.existsById(cardId);
    }

    /**
     * The method finds the user's card by its full number
     * <p><b>The current implementation will be changed</b></p>
     * @param cardNumber card number as a string to find
     * @param owner object {@link User}, to whom the sought card belongs
     * @return object {@link Card}
     * @throws CardNotFoundException If the card with the specified number is not found on the user's account
     */
    @Deprecated
    @Override
    public Card findCardByNumber(String cardNumber, User owner) {
        //TODO Temporary solution (rewrite to cardHash)
        return cardRepository.findAllByUser(owner).stream()
                .filter(card -> aesEncryption.decrypt(card.getEncryptedNumber()).equals(cardNumber))
                .findFirst()
                .orElseThrow(() -> new CardNotFoundException("You don't have a card with that number - " + cardNumber + "."));
    }

    /**
     * The method creates a list of {@link Sort.Order} objects based on the list of fields to sort and the sort direction.
     * @param sortList list of fields to sort by
     * @param sortOrder sort direction (ASC - ascending / DESC - descending)
     * @return a list of {@link Sort.Order} objects containing the sort field and direction
     * @see Sort.Order
     */
    private List<Sort.Order> createSortOrder(List<String> sortList, String sortOrder) {
        Sort.Direction sortDirection = Sort.Direction.fromString(sortOrder);
        return sortList.stream()
                .map(field -> new Sort.Order(sortDirection, field))
                .toList();
    }

    /**
     * The method checks whether the card is a duplicate
     * @param user current user
     * @param cardNumber new card number
     * @return {@code  true}, if a card with the same number already exists
     */
    private boolean isCardNumberDuplicate(User user, String cardNumber) {
        List<String> userCards = cardRepository.findEncryptedNumberByUserId(user.getId());
        return userCards.stream()
                .map(aesEncryption::decrypt)
                .anyMatch(number -> number.equals(cardNumber));
    }
}
