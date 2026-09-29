package com.example.MpApp.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;

    public JwtService(@Value("${jwt.secret}") String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT secret is not configured");
        }
        key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private static final long EXPIRATION =
            1000 * 60 * 60 * 24; // 24 Hours

    /** Claim holding the account's token counter at the time of issue. */
    public static final String CLAIM_TOKEN_VERSION = "tv";

    /*
    ===============================
    GENERATE TOKEN
    ===============================
    */

    public String generateToken(
            UserDetails userDetails) {
        return generateToken(userDetails, 0);
    }

    /**
     * @param tokenVersion the account's current counter. A later password change
     *                     increments it, which makes every token carrying the old
     *                     value fail validation.
     */
    public String generateToken(
            UserDetails userDetails,
            int tokenVersion) {

        String role = userDetails.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .filter(a -> a.startsWith("ROLE_"))
                .findFirst()
                .map(a -> a.substring("ROLE_".length()))
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user has no application role"));

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("role", role)
                .claim(CLAIM_TOKEN_VERSION, tokenVersion)
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

    /**
     * Token counter recorded in the token. Tokens minted before this claim
     * existed read as 0, which matches the column default, so they keep working
     * rather than logging every user out on deploy.
     */
    public int extractTokenVersion(String token) {
        Integer version = extractClaims(token).get(CLAIM_TOKEN_VERSION, Integer.class);
        return version == null ? 0 : version;
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