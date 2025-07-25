package com.testtask.bankcardmanagement.repository;

import com.testtask.bankcardmanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query(value = "SELECT us FROM User us WHERE us.email = :email")
    Optional<User> findUserByEmail(@Param("email") String email);

    @Query(value = "SELECT us FROM User us  WHERE us.id = :userId")
    Optional<User> findUserById(@Param("userId") Long userId);
}
