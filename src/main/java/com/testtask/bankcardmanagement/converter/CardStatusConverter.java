package com.testtask.bankcardmanagement.converter;

import com.testtask.bankcardmanagement.model.enums.CardStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class CardStatusConverter implements AttributeConverter<CardStatus, String> {
    @Override
    public String convertToDatabaseColumn(CardStatus cardStatus) {
        //TODO Do it
        return "";
    }

    @Override
    public CardStatus convertToEntityAttribute(String s) {
        //TODO Do it
        return null;
    }
}
