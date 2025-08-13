package com.testtask.bankcardmanagement.converter;

import com.testtask.bankcardmanagement.exception.other.ConvertingEnumException;
import com.testtask.bankcardmanagement.model.enums.TransactionDirection;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Convert;

import java.util.stream.Stream;

@Convert
public class TransactionDirectionConverter implements AttributeConverter<TransactionDirection, String> {

    @Override
    public String convertToDatabaseColumn(TransactionDirection transactionDirection) {
        if(transactionDirection == null)
            return null;

        return transactionDirection.name();
    }

    @Override
    public TransactionDirection convertToEntityAttribute(String strTransactionDirection) {
        return Stream.of(TransactionDirection.values())
                .filter(direction -> direction.name().equals(strTransactionDirection))
                .findFirst()
                .orElseThrow(() -> new ConvertingEnumException("Cannot correctly convert enum transactionDirection to entity: " +
                        strTransactionDirection));
    }
}
