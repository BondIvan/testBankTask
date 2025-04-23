package com.testtask.bankcardmanagement.service.card;

import com.testtask.bankcardmanagement.model.dto.CardRequest;
import com.testtask.bankcardmanagement.model.dto.CardResponse;

public interface CardService {
    CardResponse createCard(CardRequest cardRequest);
}
