package com.testtask.bankcardmanagement.controller;

import com.testtask.bankcardmanagement.exception.other.InvalidSortFieldException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

@Component
public class ValidationSortableField {
    private final Set<String> user_sortableCardFields = Set.of("id", "status", "expirationDate");
    private final Set<String> admin_sortableCardFields = Set.of("id", "user.email", "status", "expirationDate");
    private final Set<String> sortableTransactionFields = Set.of("id", "type", "amount");

    public void validCardFields(List<String> sortList) {
        validFields(user_sortableCardFields, sortList);
    }

    public void validAdminCardFields(List<String> sortList) {
        validFields(admin_sortableCardFields, sortList);
    }

    public void validTransactionFields(List<String> sortList) {
        validFields(sortableTransactionFields, sortList);
    }

    public void validFields(Set<String> checkBy, List<String> checkWhat) {
        StringJoiner sj = new StringJoiner(", ", "[", "]");
        checkWhat.forEach(field -> {
            if(!checkBy.contains(field))
                sj.add(field);
        });

        if(sj.length() > 2) // sj contains '[' and ']'
            throw new InvalidSortFieldException("Sorting cards by these fields: " + sj +" are not supported.");
    }
}
