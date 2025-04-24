package com.testtask.bankcardmanagement.service.card;

import com.testtask.bankcardmanagement.model.dto.CardParamFilter;
import com.testtask.bankcardmanagement.model.dto.CardRequest;
import com.testtask.bankcardmanagement.model.dto.CardResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CardService {
    CardResponse createCard(CardRequest cardRequest);
    Page<CardResponse> getAllCards(CardParamFilter cardParamFilter, int page, int size, List<String> sortList, String sortOrder);
}
