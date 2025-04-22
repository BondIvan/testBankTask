package com.testtask.bankcardmanagement.converter;

import com.testtask.bankcardmanagement.model.enums.TransactionType;
import jakarta.persistence.AttributeConverter;

public class TransactionTypeConverter implements AttributeConverter<TransactionType, String> {
    @Override
    public String convertToDatabaseColumn(TransactionType transactionType) {
        return "";
    }

    @Override
    public TransactionType convertToEntityAttribute(String s) {
        return null;
    }
}
