package com.testtask.bankcardmanagement.service.card;

import com.testtask.bankcardmanagement.encrypt.AESEncryption;
import com.testtask.bankcardmanagement.encrypt.hash.HashCardNumber;
import com.testtask.bankcardmanagement.exception.card.CardBalanceException;
import com.testtask.bankcardmanagement.exception.card.CardCreatingException;
import com.testtask.bankcardmanagement.exception.card.CardDuplicateException;
import com.testtask.bankcardmanagement.exception.card.CardNotFoundException;
import com.testtask.bankcardmanagement.exception.encryption.AESEncryptionException;
import com.testtask.bankcardmanagement.exception.security.HashCardNumberException;
import com.testtask.bankcardmanagement.exception.user.UserNotFoundException;
import com.testtask.bankcardmanagement.model.Card;
import com.testtask.bankcardmanagement.model.User;
import com.testtask.bankcardmanagement.model.dto.card.CardParamFilter;
import com.testtask.bankcardmanagement.model.dto.card.CardResponse;
import com.testtask.bankcardmanagement.model.dto.card.CreateCardRequest;
import com.testtask.bankcardmanagement.model.enums.CardStatus;
import com.testtask.bankcardmanagement.repository.CardRepository;
import com.testtask.bankcardmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final AESEncryption aesEncryption;
    private final UserRepository userRepository;
    private final HashCardNumber hashCardNumber;

    @Transactional
    public Card createCard(CreateCardRequest request) {
        Optional<User> optionalOwner = userRepository.findUserByEmail(request.ownerEmail());
        if(optionalOwner.isEmpty())
            throw new UserNotFoundException("User with such email not found.");

        User owner = optionalOwner.get();

        String hashForNewCard;
        try {
            hashForNewCard = hashCardNumber.hash(request.cardNumber());
        } catch (HashCardNumberException e) {
            throw new CardCreatingException("Fail hashing card number", e);
        }
        
        if(doesUserHaveCardWithThisNumber(owner.getId(), hashForNewCard))
            throw new CardDuplicateException("A card with this number already exists.");

        String encryptedNumber;
        try {
            encryptedNumber = aesEncryption.encrypt(request.cardNumber());
        } catch (AESEncryptionException e) {
            throw new CardCreatingException("Fail encrypting card number", e);
        }

        Card card = new Card();
        card.setUser(owner);
        card.setExpirationDate(request.expirationDate());
        card.setStatus(CardStatus.ACTIVE);
        card.setEncryptedNumber(encryptedNumber);
        card.setCardHash(hashForNewCard);
        card.setBalance(BigDecimal.ZERO);

        return cardRepository.save(card);
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

    public Page<Card> getAllCards(CardParamFilter filter, Pageable pageable) {
        Specification<Card> spec = CardSpecification.build(filter);
        return cardRepository.findAll(spec, pageable);
    }

    public boolean isCardBelongsToUser(Long userId, Long cardId) {
        return cardRepository.checkIsCardBelongsToUser(userId, cardId);
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
