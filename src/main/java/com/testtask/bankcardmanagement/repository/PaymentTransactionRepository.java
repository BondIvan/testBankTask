package com.testtask.bankcardmanagement.repository;

import com.testtask.bankcardmanagement.model.transaction.AbstractPaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<AbstractPaymentTransaction, Long>,
        JpaSpecificationExecutor<AbstractPaymentTransaction> {

    @Query(value = "SELECT COALESCE(SUM(apt.amount), 0) FROM AbstractPaymentTransaction apt WHERE " +
            "(TYPE(apt) = WithdrawalTransaction OR TYPE(apt) = TransferTransaction) " +
            "AND apt.sourceCard.id = :cardId " +
            "AND apt.createdAt BETWEEN :from AND :to")
    BigDecimal findAmountSpentByCardAndPeriod(
            @Param("cardId") Long cardId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );
}
