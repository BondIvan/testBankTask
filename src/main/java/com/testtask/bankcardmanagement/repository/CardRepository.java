package com.testtask.bankcardmanagement.repository;

import com.testtask.bankcardmanagement.model.Card;
import com.testtask.bankcardmanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, Long>, JpaSpecificationExecutor<Card> {
    @Query("SELECT c.encryptedNumber FROM Card c WHERE c.user.id = :ownerId")
    List<String> findEncryptedNumberByUserId(@Param("ownerId") Long ownerId);
    List<Card> findAllByUser(User user);
    boolean existsByIdAndUserId(Long cardId, Long userId);
    boolean existsById(@NonNull Long cardId);
}
