package com.testtask.bankcardmanagement.service.transaction;

import com.testtask.bankcardmanagement.model.Transaction;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionParamFilter;
import com.testtask.bankcardmanagement.model.enums.TransactionType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class TransactionSpecification {

    public static Specification<Transaction> build(TransactionParamFilter transactionParamFilter, Long userId) {
        return hasCardId(transactionParamFilter.cardId())
                .and(byUser(userId))
                .and(hasType(transactionParamFilter.type()))
                .and(hasDateAfter(transactionParamFilter.fromDate()))
                .and(hasDateBefore(transactionParamFilter.toDate()));
    }

    public static Specification<Transaction> byUser(Long userId) {
        return (root, query, criteriaBuilder) -> (userId == null) ?
                criteriaBuilder.conjunction() :
                criteriaBuilder.equal(root.join("card").get("user").get("id"), userId);
    }

    public static Specification<Transaction> hasCardId(Long cardId) {
        return (root, query, cb) -> cardId != null ?
                cb.equal(root.get("card").get("id"), cardId) :
                cb.conjunction();
    }

    public static Specification<Transaction> hasType(TransactionType type) {
        return (root, query, criteriaBuilder) -> (type != null) ?
                criteriaBuilder.equal(root.get("type"), type) :
                criteriaBuilder.conjunction();
    }

    public static Specification<Transaction> hasDateAfter(LocalDateTime afterDateTime) {
        return (root, query, criteriaBuilder) -> (afterDateTime != null) ?
                criteriaBuilder.greaterThan(root.get("transactionDate"), afterDateTime) :
                criteriaBuilder.conjunction();
    }

    public static Specification<Transaction> hasDateBefore(LocalDateTime beforeDateTime) {
        return (root, query, criteriaBuilder) -> (beforeDateTime != null) ?
                criteriaBuilder.lessThan(root.get("transactionDate"), beforeDateTime) :
                criteriaBuilder.conjunction();
    }
}
