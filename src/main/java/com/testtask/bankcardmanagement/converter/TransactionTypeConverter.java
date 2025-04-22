package com.testtask.bankcardmanagement.converter;

import com.testtask.bankcardmanagement.exception.ConvertingEnumException;
import com.testtask.bankcardmanagement.model.enums.TransactionType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Convert;

import java.util.stream.Stream;

@Convert
public class TransactionTypeConverter implements AttributeConverter<TransactionType, String> {
    @Override
    public String convertToDatabaseColumn(TransactionType transactionType) {
        if(transactionType == null)
            return null;

        return transactionType.name();
    }

    @Override
    public TransactionType convertToEntityAttribute(String strTransactionalType) {
        if(strTransactionalType == null)
            return null;

        return Stream.of(TransactionType.values())
                .filter(type -> strTransactionalType.equals(type.name()))
                .findFirst()
                .orElseThrow(() -> new ConvertingEnumException("Cannot correctly convert enum transactionalType to entity: " +
                        strTransactionalType));
    }
}
