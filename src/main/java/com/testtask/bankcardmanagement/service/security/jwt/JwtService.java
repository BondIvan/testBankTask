package com.testtask.bankcardmanagement.service.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;

/**
 * Сервис для управления JSON Web Tokens (JWT)
 */
@Service
@RequiredArgsConstructor
public class JwtService {
    @Value("${my.expired_in_days}")
    private long expired_in_days;

    @Value("${my.encryption_key}")
    private String valueForInject;

    /**
     * Статический ключ, используемый для подписи и верификации JWT
     */
    private static String KEY;

    @PostConstruct
    public void init() {
        JwtService.KEY = valueForInject;
    }

    /**
     * Метод Проверяет валидность JWT токена
     * @param token JWT токен для проверки
     * @param userDetails данные пользователя, для которого проверяется токен
     * @return {@code true}, если токен валиден, иначе {@code false}
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String userEmail = extractUserEmail(token);
        return userEmail.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Метод проверяет, истек ли срок действия токена
     * @param token JWT токен для проверки
     * @return {@code true}, если токен истек, иначе {@code false}
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Метод извлекает дату истечения срока действия из токена
     * @param token JWT токен
     * @return {@link Date} объект, представляющий дату истечения токена
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Метод генерирует JWT токен для указанного пользователя без дополнительных утверждений (claims)
     * @param userDetails данные пользователя, для которого генерируется токен
     * @return {@code String} сгенерированный JWT токен в виде строки
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Метод генерирует JWT токен для указанного пользователя с дополнительными утверждениями (claims)
     * @param extraClaims карта дополнительных утверждений, которые будут добавлены в токен
     * @param userDetails данные пользователя, для которого генерируется токен
     * @return {@code String} сгенерированный JWT токен в виде строки
     */
    public String generateToken(Map<String, Objects> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(Date.from(LocalDate.now().plusDays(expired_in_days).atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .signWith(SignatureAlgorithm.HS256, getSignInKey())
                .compact();
    }

    /**
     * Метод извлекает email пользователя из JWT токена
     * @param token JWT токен
     * @return {@code String} email пользователя из токена
     */
    public String extractUserEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Метод извлекает определенное утверждение (claim) из JWT токена с помощью предоставленной функции
     * @param token JWT токен
     * @param claimResolver функция для извлечения конкретного утверждения из {@link Claims}
     * @param <T> Тип извлекаемого утверждения
     * @return Значение утверждения указанного типа
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    /**
     * Метод извлекает все утверждения (claims) из JWT токена
     * @param token JWT токен
     * @return {@link Claims} объект, содержащий все утверждения из токена
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

    }

    /**
     * Метод генерирует или получает ключ подписи, используемый для подписи и верификации JWT
     * @return {@link SecretKey} для HMAC SHA алгоритмов.
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
