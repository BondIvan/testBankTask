package com.testtask.bankcardmanagement.converter;

import com.testtask.bankcardmanagement.exception.ConvertingEnumException;
import com.testtask.bankcardmanagement.model.enums.LimitType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Convert;

import java.util.stream.Stream;

@Convert
public class LimitTypeConverter implements AttributeConverter<LimitType, String> {
    @Override
    public String convertToDatabaseColumn(LimitType limitType) {
        if(limitType == null)
            return null;

        return limitType.name();
    }

    @Override
    public LimitType convertToEntityAttribute(String strLimitType) {
        if(strLimitType == null)
            return null;

        return Stream.of(LimitType.values())
                .filter(limitType -> strLimitType.equals(limitType.name()))
                .findFirst()
                .orElseThrow(() -> new ConvertingEnumException("Cannot correctly convert enum limitType to entity: " + strLimitType));
    }
}
