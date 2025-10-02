package org.project.backend.hubt.todo_list.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {

    @Qualifier("customStringRedisTemplate")
    private final RedisTemplate<String, String> stringRedisTemplate;

    private static final String JWT_PREFIX = "jwt:";
    private static final String REFRESH_PREFIX = "refresh:";
    private static final Duration JWT_EXPIRATION = Duration.ofDays(1); // 24 hours
    private static final Duration REFRESH_EXPIRATION = Duration.ofDays(30); // 30 days

    public void storeJwtToken(String username, String token) {
        String key = JWT_PREFIX + username;
        stringRedisTemplate.opsForValue().set(key, token, JWT_EXPIRATION);
        log.debug("JWT token stored for user: {}", username);
    }

    public String getJwtToken(String username) {
        String key = JWT_PREFIX + username;
        return stringRedisTemplate.opsForValue().get(key);
    }

    public void deleteJwtToken(String username) {
        String key = JWT_PREFIX + username;
        stringRedisTemplate.delete(key);
        log.debug("JWT token deleted for user: {}", username);
    }

    public boolean isJwtTokenValid(String username, String token) {
        String storedToken = getJwtToken(username);
        return storedToken != null && storedToken.equals(token);
    }

    public void storeRefreshToken(String username, String refreshToken) {
        String key = REFRESH_PREFIX + username;
        stringRedisTemplate.opsForValue().set(key, refreshToken, REFRESH_EXPIRATION);
        log.debug("Refresh token stored for user: {}", username);
    }

    public String getRefreshToken(String username) {
        String key = REFRESH_PREFIX + username;
        return stringRedisTemplate.opsForValue().get(key);
    }

    public void deleteRefreshToken(String username) {
        String key = REFRESH_PREFIX + username;
        stringRedisTemplate.delete(key);
        log.debug("Refresh token deleted for user: {}", username);
    }

    public boolean isRefreshTokenValid(String username, String refreshToken) {
        String storedToken = getRefreshToken(username);
        return storedToken != null && storedToken.equals(refreshToken);
    }

    public void set(String key, String value, Duration expiration) {
        stringRedisTemplate.opsForValue().set(key, value, expiration);
    }

    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }

    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    public void cleanupExpiredTokens() {
        try {
            Set<String> jwtKeys = stringRedisTemplate.keys(JWT_PREFIX + "*");
            Set<String> refreshKeys = stringRedisTemplate.keys(REFRESH_PREFIX + "*");

            if (jwtKeys != null && !jwtKeys.isEmpty()) {
                log.info("Found {} JWT tokens in Redis", jwtKeys.size());
            }

            if (refreshKeys != null && !refreshKeys.isEmpty()) {
                log.info("Found {} refresh tokens in Redis", refreshKeys.size());
            }
        } catch (Exception e) {
            log.error("Error during token cleanup", e);
        }
    }

    public void logoutUser(String username) {
        deleteJwtToken(username);
        deleteRefreshToken(username);
        log.info("User {} logged out from all devices", username);
    }
}