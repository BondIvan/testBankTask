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
    WHERE tr.card.id = :cardId AND tr.transactionDate >= :from
        AND tr.transactionDate < :to
    """)
    BigDecimal findSumTransactionsByCardIdAndPeriod(@Param("cardId") Long cardId, @Param("from")Instant from, @Param("to") Instant to);
}
