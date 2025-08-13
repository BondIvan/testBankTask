package com.testtask.bankcardmanagement.repository;

import com.testtask.bankcardmanagement.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
    @Query(value = """
    SELECT SUM(tr.amount)
    FROM Transaction tr
    WHERE tr.sourceCard.id = :sourceCardId
        AND tr.direction = 'OUTGOING'
        AND tr.createdAt >= :from
        AND tr.createdAt < :to
    """)
    BigDecimal findOutgoingSumTransactionsByCardIdAndPeriod(
            @Param("cardId") Long sourceCardId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );
}
