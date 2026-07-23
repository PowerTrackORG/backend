package com.powertrack.backend.infrastructure.security;

import com.powertrack.backend.application.auth.port.out.TokenProviderPort;
import com.powertrack.backend.domain.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de salida: implementa TokenProviderPort con jjwt (HMAC-SHA256, RNF-03).
 * También expone parseClaims(), usado directamente por JwtAuthenticationFilter
 * (parsear el header de un request entrante es un detalle de infraestructura HTTP,
 * no cruza el puerto de aplicación).
 */
@Component
public class JwtTokenAdapter implements TokenProviderPort {

    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final SecretKey signingKey;
    private final long accessTokenExpirationMinutes;
    private final long refreshTokenExpirationDays;

    public JwtTokenAdapter(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration-minutes:15}") long accessTokenExpirationMinutes,
            @Value("${jwt.refresh-token-expiration-days:30}") long refreshTokenExpirationDays) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationMinutes = accessTokenExpirationMinutes;
        this.refreshTokenExpirationDays = refreshTokenExpirationDays;
    }

    @Override
    public String generateAccessToken(User user) {
        return buildToken(user, TYPE_ACCESS, Duration.ofMinutes(accessTokenExpirationMinutes));
    }

    @Override
    public String generateRefreshToken(User user) {
        return buildToken(user, TYPE_REFRESH, Duration.ofDays(refreshTokenExpirationDays));
    }

    private String buildToken(User user, String type, Duration ttl) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim(CLAIM_EMAIL, user.getEmail())
                .claim(CLAIM_TYPE, type)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(signingKey)
                .compact();
    }

    public Optional<Claims> parseClaims(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(claims);
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public boolean isAccessToken(Claims claims) {
        return TYPE_ACCESS.equals(claims.get(CLAIM_TYPE, String.class));
    }

    @Override
    public Optional<UUID> validateRefreshTokenAndGetUserId(String refreshToken) {
        return parseClaims(refreshToken)
                .filter(claims -> TYPE_REFRESH.equals(claims.get(CLAIM_TYPE, String.class)))
                .map(claims -> UUID.fromString(claims.getSubject()));
    }
}
