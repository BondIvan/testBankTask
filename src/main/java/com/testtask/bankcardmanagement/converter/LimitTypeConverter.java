package com.testtask.bankcardmanagement.converter;

import com.testtask.bankcardmanagement.model.enums.LimitType;
import jakarta.persistence.AttributeConverter;

public class LimitTypeConverter implements AttributeConverter<LimitType, String> {
    @Override
    public String convertToDatabaseColumn(LimitType limitType) {
        return "";
    }

    @Override
    public LimitType convertToEntityAttribute(String s) {
        return null;
    }
}
