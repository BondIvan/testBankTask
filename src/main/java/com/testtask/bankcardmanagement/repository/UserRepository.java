package com.testtask.bankcardmanagement.repository;

import com.testtask.bankcardmanagement.model.User;
import com.testtask.bankcardmanagement.repository.query.UserQueries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByEmail(String email);
    boolean existsByEmail(String email);

    @Query(value = UserQueries.CHANGE_USER_EMAIL_BY_USER_ID_QUERY, nativeQuery = true)
    User changeUserEmailByUserId(@Param("userId") Long userId, @Param("newEmail") String newEmail);

    @Query(value = UserQueries.CHANGE_PASSWORD_BY_USER_ID_QUERY, nativeQuery = true)
    User changeUserPasswordByUserId(@Param("userId") Long userId, @Param("newPassword") String newPassword);
}
