package com.testtask.bankcardmanagement.model.mapper;

import com.testtask.bankcardmanagement.encrypt.AESEncryption;
import com.testtask.bankcardmanagement.model.Card;
import com.testtask.bankcardmanagement.model.dto.card.CardResponse;
import com.testtask.bankcardmanagement.model.dto.limit.LimitResponse;
import com.testtask.bankcardmanagement.model.dto.user.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class CardMapper {
    private final AESEncryption aesEncryption;
    private final UserMapper userMapper;
    private final LimitMapper limitMapper;

    public CardResponse toCardResponse(Card card) {
        UserResponse userResponse = userMapper.toUserResponse(card.getUser());
        List<LimitResponse> limitResponse = limitMapper.toListLimitResponse(card.getLimits());
        return new CardResponse(
                maskCardNumber(card.getEncryptedNumber()),
                card.getExpirationDate(),
                userResponse,
                card.getStatus(),
                card.getBalance(),
                limitResponse
        );
    }

    private String maskCardNumber(String encryptedNumber) {
        String decryptedNumber = aesEncryption.decrypt(encryptedNumber);
        return "**** **** **** " + decryptedNumber.substring(12);
    }

}
