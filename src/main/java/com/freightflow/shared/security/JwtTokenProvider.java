package com.freightflow.shared.security;

import com.freightflow.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final SecretKey secretKey;
    private final long expirationMs;
    private final long refreshExpirationMs;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        // Garante que a secret tem pelo menos 256 bits para HMAC-SHA256
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                    "JWT secret must be at least 256 bits (32 bytes). Current: " + keyBytes.length + " bytes");
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = jwtProperties.getExpirationMs();
        this.refreshExpirationMs = jwtProperties.getRefreshExpirationMs();
    }

    /**
     * Gera access token JWT com: id, email, tenantId, role e customerId (opcional).
     * customerId é preenchido apenas para usuarios com role CLIENT.
     */
    public String generateAccessToken(UserPrincipal principal) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(expirationMs);

        var builder = Jwts.builder()
                .subject(principal.getId().toString())
                .claim("email", principal.getEmail())
                .claim("tenantId", principal.getTenantId().toString())
                .claim("role", principal.getRole());

        if (principal.getCustomerId() != null) {
            builder.claim("customerId", principal.getCustomerId().toString());
        }

        return builder
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Gera um refresh token com expiracao mais longa (7 dias default).
     */
    public String generateRefreshToken(UserPrincipal principal) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(refreshExpirationMs);

        return Jwts.builder()
                .subject(principal.getId().toString())
                .claim("type", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Valida o token e extrai as claims. Retorna null se invalido/expirado.
     */
    public Claims validateAndGetClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException ex) {
            log.warn("JWT expired: {}", ex.getMessage());
            return null;
        } catch (JwtException ex) {
            log.warn("Invalid JWT: {}", ex.getMessage());
            return null;
        }
    }

    /**
     * Extrai UserPrincipal a partir das claims do token.
     * customerId pode ser null (roles ADMIN/OPERATOR/VIEWER nao carregam).
     */
    public UserPrincipal getUserPrincipalFromClaims(Claims claims) {
        UUID userId   = UUID.fromString(claims.getSubject());
        String email  = claims.get("email", String.class);
        UUID tenantId = UUID.fromString(claims.get("tenantId", String.class));
        String role   = claims.get("role", String.class);

        String customerIdStr = claims.get("customerId", String.class);
        UUID customerId = (customerIdStr != null) ? UUID.fromString(customerIdStr) : null;

        return UserPrincipal.fromToken(userId, email, tenantId, role, customerId);
    }
}
