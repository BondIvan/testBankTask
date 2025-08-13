package com.testtask.bankcardmanagement.service.transaction;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class TransactionService {
    private List<Sort.Order> createSortOrder(List<String> sortList, String sortOrder) {
        Sort.Direction sortDirection = Sort.Direction.fromString(sortOrder);
        return sortList.stream()
                .map(field -> new Sort.Order(sortDirection, field))
                .toList();
    }
}
