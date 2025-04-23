package com.testtask.bankcardmanagement.service.card.impl;

import com.testtask.bankcardmanagement.encrypt.AESEncryption;
import com.testtask.bankcardmanagement.model.Card;
import com.testtask.bankcardmanagement.model.User;
import com.testtask.bankcardmanagement.model.dto.CardRequest;
import com.testtask.bankcardmanagement.model.dto.CardResponse;
import com.testtask.bankcardmanagement.model.enums.CardStatus;
import com.testtask.bankcardmanagement.model.mapper.CardMapper;
import com.testtask.bankcardmanagement.repository.CardRepository;
import com.testtask.bankcardmanagement.repository.UserRepository;
import com.testtask.bankcardmanagement.service.card.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

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

    @Override
    public CardResponse createCard(CardRequest cardRequest) {
        Optional<User> optionalUser = userRepository.findUserByEmail(cardRequest.ownerEmail());
        if(optionalUser.isEmpty())
            throw new RuntimeException("User with such email not found");

        User owner = optionalUser.get();

        if(isCardNumberDuplicate(owner, cardRequest.cardNumber()))
            throw new RuntimeException("A card with this number already exists.");

        Card card = new Card();
        card.setUser(owner);
        card.setEncryptedNumber(aesEncryption.encrypt(cardRequest.cardNumber()));
        card.setExpirationDate(cardRequest.expirationDate());
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.ZERO);

        try {
            Card savedCard = cardRepository.save(card);
            return cardMapper.toCardResponse(savedCard);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("A card with this number already exists in the database.");
        }
    }

    private boolean isCardNumberDuplicate(User user, String cardNumber) {
        List<String> userCards = cardRepository.findEncryptedNumberByUserId(user.getId());
        System.out.println("UserId: " + user.getId() + "userCards: " + userCards);
        return userCards.stream()
                .map(aesEncryption::decrypt)
                .anyMatch(number -> number.equals(cardNumber));
    }
}
