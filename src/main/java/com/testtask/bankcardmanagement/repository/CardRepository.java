package com.testtask.bankcardmanagement.repository;

import com.testtask.bankcardmanagement.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID>, JpaSpecificationExecutor<Card> {
    @Query("SELECT c.encryptedNumber FROM Card c WHERE c.user.id = :ownerId")
    List<String> findEncryptedNumberByUserId(@Param("ownerId") Long ownerId);
}
