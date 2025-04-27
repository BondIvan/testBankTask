package com.testtask.bankcardmanagement.converter;

import com.testtask.bankcardmanagement.exception.other.ConvertingEnumException;
import com.testtask.bankcardmanagement.model.enums.CardStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Stream;

@Converter
public class CardStatusConverter implements AttributeConverter<CardStatus, String> {
    @Override
    public String convertToDatabaseColumn(CardStatus cardStatus) {
        if(cardStatus == null)
            return null;

        return cardStatus.name();
    }

    @Override
    public CardStatus convertToEntityAttribute(String strCardStatus) {
        if(strCardStatus == null)
            return null;

        return Stream.of(CardStatus.values())
                .filter(status -> strCardStatus.equals(status.name()))
                .findFirst()
                .orElseThrow(() -> new ConvertingEnumException("Cannot correctly convert enum cardStatus to entity: " + strCardStatus));
    }
}
