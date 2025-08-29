package com.testtask.bankcardmanagement.service.card;

import com.testtask.bankcardmanagement.encrypt.AESEncryption;
import com.testtask.bankcardmanagement.encrypt.hash.HashCardNumber;
import com.testtask.bankcardmanagement.exception.card.CardBalanceException;
import com.testtask.bankcardmanagement.exception.card.CardDuplicateException;
import com.testtask.bankcardmanagement.exception.card.CardNotFoundException;
import com.testtask.bankcardmanagement.exception.user.UserNotFoundException;
import com.testtask.bankcardmanagement.model.Card;
import com.testtask.bankcardmanagement.model.User;
import com.testtask.bankcardmanagement.model.dto.card.CardParamFilter;
import com.testtask.bankcardmanagement.model.dto.card.CardResponse;
import com.testtask.bankcardmanagement.model.dto.card.CreateCardRequest;
import com.testtask.bankcardmanagement.model.enums.CardStatus;
import com.testtask.bankcardmanagement.model.mapper.CardMapper;
import com.testtask.bankcardmanagement.repository.CardRepository;
import com.testtask.bankcardmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final AESEncryption aesEncryption;
    private final UserRepository userRepository;
    private final CardMapper cardMapper;
    private final HashCardNumber hashCardNumber;

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

    public Card getCardWithLimitsByUser(Long userId, Long cardId) {
        return cardRepository.findCardWithLimitsByUserIdAndCardId(userId, cardId)
                .orElseThrow(() -> new CardNotFoundException("The user does not have a card with such card id."));
    }

    public Card getCardByUserAndNumber(Long userId, String cardNumber) {
        String cardHash = hashCardNumber.hash(cardNumber);

        return cardRepository.findCardByUserIdAndCardHash(userId, cardHash)
                .orElseThrow(() -> new CardNotFoundException("The user does not have a card with such number."));
    }

    public Page<CardResponse> getAllCards(CardParamFilter filter, Pageable pageable) {
        Specification<Card> cardSpec = CardSpecification.build(filter);

        List<CardResponse> foundCards = cardRepository.findAll(cardSpec, pageable).stream()
                .map(cardMapper::toCardResponse)
                .toList();

        return new PageImpl<>(
                foundCards,
                pageable,
                foundCards.size());
    }

    public CardResponse blockCard(Long id) {
        return null;
    }

    public CardResponse activateCard(Long id) {
        return null;
    }

    @Transactional
    public boolean deleteCardById(Long cardId) {
        Card card = cardRepository.findCardById(cardId)
                        .orElseThrow(() -> new CardNotFoundException("A card with such id not found."));

        if(card.getBalance().signum() != 0)
            throw new CardBalanceException("Cannot delete a card with a non-zero amount.");

        cardRepository.delete(card);

        return true;
    }

    private boolean doesUserHaveCardWithThisNumber(Long userId, String hashNewNumber) {
        return cardRepository.existByUserAndHashNumber(userId, hashNewNumber);
    }
}
