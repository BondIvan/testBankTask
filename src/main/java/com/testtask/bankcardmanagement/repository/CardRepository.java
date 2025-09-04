package com.testtask.bankcardmanagement.repository;

import com.testtask.bankcardmanagement.model.Card;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long>, JpaSpecificationExecutor<Card> {
    @Query(value = "SELECT COUNT(c) > 0 FROM Card c WHERE c.user.id = :userId AND c.cardHash = :hashNumber")
    boolean existByUserAndHashNumber(@Param("userId") Long userId, @Param("hashNumber") String hashNumber);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM Card c WHERE c.id = :cardId AND c.user.id = :userId)")
    boolean checkIsCardBelongsToUser(@Param("userId") Long userId, @Param("cardId") Long cardId);

    boolean existsById(Long cardId);

    @Query(value = "SELECT c FROM Card c WHERE c.id = :cardId")
    Optional<Card> findCardById(@Param("cardId") Long cardId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "SELECT c FROM Card c WHERE c.id = :cardId")
    Optional<Card> findCardByIdForUpdate(@Param("cardId") Long cardId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "SELECT c FROM Card c LEFT JOIN FETCH c.limits WHERE c.id = :cardId")
    Optional<Card> findCardWithLimitsByIdForUpdate(@Param("cardId") Long cardId);

    @Query(value = "SELECT c FROM Card c JOIN c.user WHERE c.user.id = :userId AND c.cardHash = :cardHash")
    Optional<Card> findCardByUserIdAndCardHash(@Param("userId") Long userId, @Param("cardHash") String cardHash);

    @Query(value = "SELECT c FROM Card c LEFT JOIN FETCH c.limits JOIN c.user WHERE c.user.id = :userId AND c.id = :cardId")
    Optional<Card> findCardWithLimitsByUserIdAndCardId(@Param("userId") Long userId, @Param("cardId") Long cardId);

    @Override
    @EntityGraph(attributePaths = "user")
    Page<Card> findAll(Specification<Card> spec, Pageable pageable);
}
