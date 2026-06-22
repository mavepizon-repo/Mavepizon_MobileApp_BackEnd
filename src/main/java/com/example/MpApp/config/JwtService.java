package com.example.MpApp.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private static final String SECRET_KEY =
            "MavePizonSecretKeyForJwtTokenGeneration2026SpringBootJWTAuthenticationSecureKey";

    private final SecretKey key =
            Keys.hmacShaKeyFor(
                    SECRET_KEY.getBytes(StandardCharsets.UTF_8)
            );

    private static final long EXPIRATION =
            1000 * 60 * 60 * 24; // 24 Hours

    /*
    ===============================
    GENERATE TOKEN
    ===============================
    */

    public String generateToken(
            UserDetails userDetails) {

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + EXPIRATION
                        )
                )
                .signWith(key)
                .compact();
    }

    /*
    ===============================
    EXTRACT EMAIL
    ===============================
    */

    public String extractEmail(
            String token) {

        return extractClaims(token)
                .getSubject();
    }

    public String extractUsername(String token) {
        return extractEmail(token);
    }

    // Public method to extract custom single role claim
    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    /*
    ===============================
    EXTRACT CLAIMS
    ===============================
    */

    private Claims extractClaims(
            String token) {

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /*
    ===============================
    TOKEN VALIDATION
    ===============================
    */

    public boolean isTokenValid(
            String token,
            UserDetails userDetails) {

        String email =
                extractEmail(token);

        return email.equals(
                userDetails.getUsername())
                &&
                !isTokenExpired(token);
    }

    /*
    ===============================
    TOKEN EXPIRY CHECK
    ===============================
    */

    private boolean isTokenExpired(
            String token) {

        return extractClaims(token)
                .getExpiration()
                .before(new Date());
    }
}