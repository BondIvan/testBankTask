package com.testtask.bankcardmanagement.repository;

import com.testtask.bankcardmanagement.model.Limit;
import com.testtask.bankcardmanagement.model.enums.LimitType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LimitRepository extends JpaRepository<Limit, Long> {
    @Query(value = "SELECT lim FROM Limit lim WHERE lim.card.id = :cardId AND lim.limitType = :limitType")
    Optional<Limit> findLimitByCardIdAndLimitType(@Param("cardId") Long cardId, @Param("limitType") LimitType limitType);
}
