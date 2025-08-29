package com.testtask.bankcardmanagement.service.transaction;

import com.testtask.bankcardmanagement.model.Card;
import com.testtask.bankcardmanagement.model.transaction.AbstractPaymentTransaction;
import com.testtask.bankcardmanagement.model.transaction.ReplenishmentTransaction;
import com.testtask.bankcardmanagement.model.transaction.TransferTransaction;
import com.testtask.bankcardmanagement.model.transaction.WithdrawalTransaction;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;

public class PaymentTransactionSpecification {

    public static Specification<AbstractPaymentTransaction> byCardId(Long cardId) {
        return (root, query, cb) -> {

            Root<ReplenishmentTransaction> replenishmentRoot = cb.treat(root, ReplenishmentTransaction.class);
            Predicate replenishmentPredicate = cb.equal(replenishmentRoot.get("targetCard").get("id"), cardId);

            Root<WithdrawalTransaction> withdrawalRoot = cb.treat(root, WithdrawalTransaction.class);
            Predicate withdrawalPredicate = cb.equal(withdrawalRoot.get("sourceCard").get("id"), cardId);

            Root<TransferTransaction> transferRoot = cb.treat(root, TransferTransaction.class);
            Predicate transferPredicate = cb.or(
                    cb.equal(transferRoot.get("sourceCard").get("id"), cardId),
                    cb.equal(transferRoot.get("targetCard").get("id"), cardId)
            );

            return cb.or(replenishmentPredicate, withdrawalPredicate, transferPredicate);
        };
    }

    public static Specification<AbstractPaymentTransaction> byUserId(Long userId) {
        return (root, query, cb) -> {
            if(userId == null)
                return cb.conjunction();

            Subquery<Long> cardIdsBelongsUserIdSubquery = query.subquery(Long.class);
            Root<Card> cardRoot = cardIdsBelongsUserIdSubquery.from(Card.class);
            cardIdsBelongsUserIdSubquery.select(cardRoot.get("id")).where(cb.equal(cardRoot.get("user").get("id"), userId));

            Predicate sourceCardInUserCardPredicate = cb.in(root.get("sourceCard").get("id")).value(cardIdsBelongsUserIdSubquery);
            Predicate targetCardInUserCardPredicate = cb.in(root.get("targetCard").get("id")).value(cardIdsBelongsUserIdSubquery);

            return cb.or(sourceCardInUserCardPredicate, targetCardInUserCardPredicate);
        };
    }

    public static Specification<AbstractPaymentTransaction> byCreatedAt(Instant from, Instant to) {
        return (root, query, cb) -> {
            Predicate fromCreatedAtPredicate = (from != null) ?
                    cb.greaterThanOrEqualTo(root.get("createdAt"), from) :
                    cb.conjunction();

            Predicate toCreatedAtPredicate = (to != null) ?
                    cb.lessThan(root.get("createdAt"), to) :
                    cb.conjunction();

            return cb.and(fromCreatedAtPredicate, toCreatedAtPredicate);
        };
    }

    public static Specification<AbstractPaymentTransaction> byAmount(BigDecimal from, BigDecimal to) {
        return (root, query, cb) -> {
            Predicate fromAmountPredicate = (from != null) ?
                    cb.greaterThanOrEqualTo(root.get("amount"), from) :
                    cb.conjunction();

            Predicate toAmountPredicate = (to != null) ?
                    cb.lessThan(root.get("amount"), to.add(BigDecimal.ONE)) :
                    cb.conjunction();

            return cb.and(fromAmountPredicate, toAmountPredicate);
        };
    }
}
