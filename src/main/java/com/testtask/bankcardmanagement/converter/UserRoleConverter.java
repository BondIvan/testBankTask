package com.testtask.bankcardmanagement.converter;

import com.testtask.bankcardmanagement.model.enums.UserRole;
import jakarta.persistence.AttributeConverter;

public class UserRoleConverter implements AttributeConverter<UserRole, String> {
    @Override
    public String convertToDatabaseColumn(UserRole userRole) {
        return "";
    }

    @Override
    public UserRole convertToEntityAttribute(String s) {
        return null;
    }
}
