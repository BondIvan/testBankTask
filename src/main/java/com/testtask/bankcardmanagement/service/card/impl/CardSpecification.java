package com.testtask.bankcardmanagement.service.card.impl;

import com.testtask.bankcardmanagement.model.Card;
import com.testtask.bankcardmanagement.model.dto.card.CardParamFilter;
import com.testtask.bankcardmanagement.model.enums.CardStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class CardSpecification {

    public static Specification<Card> build(CardParamFilter cardParamFilter) {
        return hasStatus(cardParamFilter.status())
                .and(hasUserEmail(cardParamFilter.userEmail()));
    }

    public static Specification<Card> hasStatus(CardStatus cardStatus) {
        return (root, query, criteriaBuilder) -> (cardStatus != null) ?
                criteriaBuilder.equal(root.get("status"), cardStatus) :
                criteriaBuilder.conjunction();
    }

    public static Specification<Card> hasUserEmail(String email) {
        return (root, query, criteriaBuilder) -> (email != null) ?
                criteriaBuilder.equal(root.join("user").get("email"), email) :
                criteriaBuilder.conjunction();
    }

}
