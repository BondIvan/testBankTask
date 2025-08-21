package com.testtask.bankcardmanagement.service.transaction;

import com.testtask.bankcardmanagement.model.Card;
import com.testtask.bankcardmanagement.model.Transaction;
import com.testtask.bankcardmanagement.model.User;
import com.testtask.bankcardmanagement.model.dto.transaction.TransactionParamFilter;
import com.testtask.bankcardmanagement.model.enums.TransactionType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public class TransactionSpecification {

    public static Specification<Transaction> build(TransactionParamFilter transactionParamFilter, Long userId) {
        return byUserEmail(transactionParamFilter.userEmail())
                .and(hasType(transactionParamFilter.type()))
                .and(hasDateAfter(transactionParamFilter.fromDate()))
                .and(hasDateBefore(transactionParamFilter.toDate()));
    }

    public static Specification<Transaction> byUserEmail(String userEmail) {
        return (root, query, criteriaBuilder) -> {
            if(userEmail == null)
                return criteriaBuilder.conjunction();

            Predicate sourceCardUser = criteriaBuilder.equal(root.join("sourceCard").get("user").get("email"), userEmail);
            Predicate targetCardUser = criteriaBuilder.equal(root.join("targetCard").get("user").get("email"), userEmail);

            return criteriaBuilder.or(sourceCardUser, targetCardUser);
        };

//        return (root, query, criteriaBuilder) -> (userEmail == null) ?
//                criteriaBuilder.conjunction() :
//                criteriaBuilder.equal(root.join("sourceCard").get("user").get("email"), userEmail);
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

    public static Specification<Transaction> hasDateAfter(Instant after) {
        return (root, query, criteriaBuilder) -> (after != null) ?
                criteriaBuilder.greaterThan(root.get("createdAt"), after) :
                criteriaBuilder.conjunction();
    }
//TODO Make it BETWEEN
    public static Specification<Transaction> hasDateBefore(Instant before) {
        return (root, query, criteriaBuilder) -> (before != null) ?
                criteriaBuilder.lessThan(root.get("createdAt"), before) :
                criteriaBuilder.conjunction();
    }
}
