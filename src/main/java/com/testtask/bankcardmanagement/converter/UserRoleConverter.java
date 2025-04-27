package com.testtask.bankcardmanagement.converter;

import com.testtask.bankcardmanagement.exception.other.ConvertingEnumException;
import com.testtask.bankcardmanagement.model.enums.UserRole;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Convert;

import java.util.stream.Stream;

@Convert
public class UserRoleConverter implements AttributeConverter<UserRole, String> {
    @Override
    public String convertToDatabaseColumn(UserRole userRole) {
        if(userRole == null)
            return null;

        return userRole.name();
    }

    @Override
    public UserRole convertToEntityAttribute(String strUserRole) {
        if(strUserRole == null)
            return null;

        return Stream.of(UserRole.values())
                .filter(role -> strUserRole.equals(role.name()))
                .findFirst()
                .orElseThrow(() -> new ConvertingEnumException("Cannot correctly convert enum userRole to entity: " + strUserRole));
    }
}
