package org.project.backend.hubt.todo_list.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret:todoSecretKey}")
    private String jwtSecret;

    @Value("${app.jwt.expiration:86400000}")
    private Long jwtExpiration;

    @Value("${app.jwt.refresh-expiration:2592000000}")
    private Long refreshExpiration;

    public String generateToken(String username) {
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpiration);

        return JWT.create()
                .withSubject(username)
                .withIssuedAt(new Date())
                .withExpiresAt(expiryDate)
                .sign(Algorithm.HMAC256(jwtSecret));
    }

    public String getUsernameFromToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(jwtSecret)).build();
            DecodedJWT decodedJWT = verifier.verify(token);
            return decodedJWT.getSubject();
        } catch (JWTVerificationException e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    public String generateRefreshToken(String username) {
        Date expiryDate = new Date(System.currentTimeMillis() + refreshExpiration);

        return JWT.create()
                .withSubject(username)
                .withIssuedAt(new Date())
                .withExpiresAt(expiryDate)
                .withClaim("type", "refresh")
                .sign(Algorithm.HMAC256(jwtSecret));
    }

    public boolean validateToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(jwtSecret)).build();
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(jwtSecret)).build();
            DecodedJWT decodedJWT = verifier.verify(token);
            return "refresh".equals(decodedJWT.getClaim("type").asString());
        } catch (JWTVerificationException e) {
            return false;
        }
    }
}