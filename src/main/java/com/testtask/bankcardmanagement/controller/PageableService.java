package com.testtask.bankcardmanagement.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PageableService {
    private final SortableFieldService sortableFieldService;

    public Pageable createCardPageable(int page, int size, List<String> sortList, String sortOrder) {
        sortableFieldService.validCardFields(sortList);
        return create(page, size, sortList, sortOrder);
    }

    public Pageable createAdminCardPageable(int page, int size, List<String> sortList, String sortOrder) {
        sortableFieldService.validAdminCardFields(sortList);
        return create(page, size, sortList, sortOrder);
    }

    public Pageable createTransactionPageable(int page, int size, List<String> sortList, String sortOrder) {
        sortableFieldService.validTransactionFields(sortList);
        return create(page, size, sortList, sortOrder);
    }

    private Pageable create(int page, int size, List<String> sortList, String sortOrder) {
        List<Sort.Order> sorted = sortableFieldService.createSortOrder(sortList, sortOrder);
        return PageRequest.of(page, size, Sort.by(sorted));
    }
}
