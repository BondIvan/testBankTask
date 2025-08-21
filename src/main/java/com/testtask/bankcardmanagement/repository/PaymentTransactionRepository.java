package com.testtask.bankcardmanagement.repository;

import com.testtask.bankcardmanagement.model.transaction.AbstractPaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<AbstractPaymentTransaction, Long>,
        JpaSpecificationExecutor<AbstractPaymentTransaction> {

    //TODO Нужна сумма транзакций по карте за период
//    BigDecimal getSumAllTransactionsByCardIdAndPeriod(
//            @Param("cardId") Long sourceCardId,
//            @Param("from") Instant from,
//            @Param("to") Instant to
//    );
}
