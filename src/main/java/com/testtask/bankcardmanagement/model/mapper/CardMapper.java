package com.testtask.bankcardmanagement.model.mapper;

import com.testtask.bankcardmanagement.encrypt.AESEncryption;
import com.testtask.bankcardmanagement.model.Card;
import com.testtask.bankcardmanagement.model.dto.CardRequest;
import com.testtask.bankcardmanagement.model.dto.CardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CardMapper {
    private final AESEncryption aesEncryption;

    public Card fromCardRequest(CardRequest cardRequest) {
        Card card = new Card();
        card.setEncryptedNumber(aesEncryption.encrypt(cardRequest.cardNumber()));
        card.setUser(cardRequest.owner());
        card.setExpirationDate(cardRequest.expirationDate());

        return card;
    }

    public CardResponse toCardResponse(Card card) {
        return new CardResponse(
                maskCardNumber(card.getEncryptedNumber()),
                card.getExpirationDate(),
                card.getUser(),
                card.getStatus(),
                card.getBalance()
        );
    }

    private String maskCardNumber(String encryptedNumber) {
        String decryptedNumber = aesEncryption.decrypt(encryptedNumber);
        return "**** **** **** " + decryptedNumber.substring(12);
    }

}
