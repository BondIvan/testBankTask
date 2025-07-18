package com.testtask.bankcardmanagement.repository.query;

public class UserQueries {
    public final static String CHANGE_USER_EMAIL_BY_USER_ID_QUERY = """
            UPDATE users
            SET email = :newEmail
            WHERE id = :userId
            """;

    public final static String CHANGE_PASSWORD_BY_USER_ID_QUERY = """
            UPDATE users
            SET password = :newPassword
            WHERE id = :userId
            """;
}
