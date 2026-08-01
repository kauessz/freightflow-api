package com.freightflow.shared.security.platform;

import com.freightflow.config.PlatformJwtProperties;
import com.freightflow.modules.platform.PlatformRole;
import com.freightflow.modules.platform.PlatformUserStatus;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Component
public class PlatformJwtService {

    public static final String TOKEN_TYPE = "PLATFORM";
    private static final Logger log = LoggerFactory.getLogger(PlatformJwtService.class);

    private final PlatformJwtProperties properties;

    public PlatformJwtService(PlatformJwtProperties properties) {
        this.properties = properties;
    }

    public String generateAccessToken(PlatformPrincipal principal) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(properties.getExpirationMs());

        return Jwts.builder()
                .subject(principal.getId().toString())
                .issuer(properties.getIssuer())
                .audience().add(properties.getAudience()).and()
                .claim("token_type", TOKEN_TYPE)
                .claim("email", principal.getEmail())
                .claim("platformRole", principal.getRole())
                .claim("status", principal.getStatus())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey())
                .compact();
    }

    public Claims validateAndGetClaims(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (!TOKEN_TYPE.equals(claims.get("token_type", String.class))) {
                log.warn("Rejected JWT without platform token_type");
                return null;
            }
            if (!properties.getIssuer().equals(claims.getIssuer())) {
                log.warn("Rejected platform JWT with unexpected issuer");
                return null;
            }
            Set<String> audience = claims.getAudience();
            if (audience == null || audience.size() != 1 || !audience.contains(properties.getAudience())) {
                log.warn("Rejected platform JWT with unexpected audience");
                return null;
            }
            return claims;
        } catch (ExpiredJwtException ex) {
            log.warn("Platform JWT expired: {}", ex.getMessage());
            return null;
        } catch (IllegalStateException ex) {
            log.warn("Platform JWT validation unavailable: {}", ex.getMessage());
            return null;
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("Invalid platform JWT: {}", ex.getMessage());
            return null;
        }
    }

    public PlatformPrincipal getPrincipalFromClaims(Claims claims) {
        try {
            UUID userId = UUID.fromString(claims.getSubject());
            if (claims.get("tenantId") != null || claims.get("customerId") != null || claims.get("role") != null) {
                log.warn("Rejected platform JWT with tenant-scoped claims");
                return null;
            }

            String email = claims.get("email", String.class);
            String roleClaim = claims.get("platformRole", String.class);
            String statusClaim = claims.get("status", String.class);

            PlatformRole role = PlatformRole.valueOf(roleClaim);
            PlatformUserStatus status = PlatformUserStatus.valueOf(statusClaim);
            return PlatformPrincipal.fromToken(userId, email, role.name(), status.name());
        } catch (IllegalArgumentException ex) {
            log.warn("Rejected platform JWT with invalid principal claims: {}", ex.getMessage());
            return null;
        }
    }

    public boolean isConfigured() {
        return StringUtils.hasText(properties.getSecret()) && properties.getSecret().getBytes(StandardCharsets.UTF_8).length >= 32;
    }

    private SecretKey secretKey() {
        String secret = properties.getSecret();
        if (!StringUtils.hasText(secret)) {
            throw new IllegalStateException("Platform JWT secret is not configured. Set PLATFORM_JWT_SECRET before using platform authentication.");
        }
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("Platform JWT secret must be at least 32 bytes.");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
