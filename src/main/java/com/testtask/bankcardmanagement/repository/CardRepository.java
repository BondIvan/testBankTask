package com.testtask.bankcardmanagement.repository;

import com.testtask.bankcardmanagement.model.Card;
import com.testtask.bankcardmanagement.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long>, JpaSpecificationExecutor<Card> {
    @Query(value = "SELECT COUNT(c) > 0 FROM Card c WHERE c.user.id = :userId AND c.cardHash = :hashNumber")
    boolean existByUserAndHashNumber(@Param("userId") Long userId, @Param("hashNumber") String hashNumber);

    @Query(value = "SELECT c FROM Card c WHERE c.id = :cardId")
    Optional<Card> findCardById(@Param("cardId") Long cardId);

    @Query(value = "SELECT COUNT(c) > 0 FROM Card c WHERE c.id = :cardId")
    boolean existByCardId(@Param("cardId") Long cardId);

    @Query(value = "SELECT c FROM Card c JOIN c.user WHERE c.user.id = :userId AND c.id = :cardId")
    Optional<Card> findCardByUserIdAndCardId(@Param("userId") Long userId, @Param("cardId") Long cardId);

    @Query(value = "SELECT c FROM Card c LEFT JOIN FETCH c.limits JOIN c.user WHERE c.user.id = :userId AND c.id = :cardId")
    Optional<Card> findCardWithLimitsByUserIdAndCardId(@Param("userId") Long userId, @Param("cardId") Long cardId);

    @Override
    @EntityGraph(attributePaths = "user")
    Page<Card> findAll(Specification<Card> spec, Pageable pageable);

    @Query("SELECT c.encryptedNumber FROM Card c WHERE c.user.id = :ownerId")
    List<String> findEncryptedNumberByUserId(@Param("ownerId") Long ownerId);
    List<Card> findAllByUser(User user);
    boolean existsByIdAndUserId(Long cardId, Long userId);
    boolean existsById(@NonNull Long cardId);
}
