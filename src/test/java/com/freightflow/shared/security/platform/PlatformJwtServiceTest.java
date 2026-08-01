package com.freightflow.shared.security.platform;

import com.freightflow.config.PlatformJwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PlatformJwtService")
class PlatformJwtServiceTest {

    @Test
    @DisplayName("geraTokenComClaimsPlatformESemTenantOuCustomer")
    void geraTokenComClaimsPlatformESemTenantOuCustomer() {
        PlatformJwtProperties properties = properties(
                "platform-test-secret-key-minimum-32-bytes-long!!",
                "freightflow-platform-test",
                "freightflow-platform-api-test",
                60_000
        );
        PlatformJwtService service = new PlatformJwtService(properties);
        PlatformPrincipal principal = new PlatformPrincipal(
                UUID.randomUUID(),
                "platform@freightflow.com",
                null,
                "PLATFORM_ADMIN",
                "ACTIVE"
        );

        String token = service.generateAccessToken(principal);
        var claims = service.validateAndGetClaims(token);

        assertThat(claims).isNotNull();
        assertThat(claims.getAudience()).isEqualTo(Set.of("freightflow-platform-api-test"));
        assertThat(claims.get("token_type", String.class)).isEqualTo(PlatformJwtService.TOKEN_TYPE);
        assertThat(claims.get("platformRole", String.class)).isEqualTo("PLATFORM_ADMIN");
        assertThat(claims.get("tenantId", String.class)).isNull();
        assertThat(claims.get("customerId", String.class)).isNull();
        assertThat(claims.get("role", String.class)).isNull();
    }

    @Test
    @DisplayName("rejeitaTokenExpirado")
    void rejeitaTokenExpirado() {
        PlatformJwtProperties properties = properties(
                "platform-test-secret-key-minimum-32-bytes-long!!",
                "freightflow-platform-test",
                "freightflow-platform-api-test",
                60_000
        );
        PlatformJwtService service = new PlatformJwtService(properties);

        String token = Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .issuer(properties.getIssuer())
                .audience().add(properties.getAudience()).and()
                .claim("token_type", PlatformJwtService.TOKEN_TYPE)
                .claim("email", "platform@freightflow.com")
                .claim("platformRole", "PLATFORM_ADMIN")
                .claim("status", "ACTIVE")
                .issuedAt(Date.from(Instant.now().minusSeconds(120)))
                .expiration(Date.from(Instant.now().minusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertThat(service.validateAndGetClaims(token)).isNull();
    }

    @Test
    @DisplayName("rejeitaIssuerAudienceOuAssinaturaInvalidos")
    void rejeitaIssuerAudienceOuAssinaturaInvalidos() {
        PlatformJwtProperties properties = properties(
                "platform-test-secret-key-minimum-32-bytes-long!!",
                "freightflow-platform-test",
                "freightflow-platform-api-test",
                60_000
        );
        PlatformJwtService service = new PlatformJwtService(properties);
        Instant now = Instant.now();

        String wrongIssuer = Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .issuer("another-issuer")
                .audience().add(properties.getAudience()).and()
                .claim("token_type", PlatformJwtService.TOKEN_TYPE)
                .claim("email", "platform@freightflow.com")
                .claim("platformRole", "PLATFORM_ADMIN")
                .claim("status", "ACTIVE")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8)))
                .compact();

        String wrongAudience = Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .issuer(properties.getIssuer())
                .audience().add("another-audience").and()
                .claim("token_type", PlatformJwtService.TOKEN_TYPE)
                .claim("email", "platform@freightflow.com")
                .claim("platformRole", "PLATFORM_ADMIN")
                .claim("status", "ACTIVE")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8)))
                .compact();

        String wrongSignature = Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .issuer(properties.getIssuer())
                .audience().add(properties.getAudience()).and()
                .claim("token_type", PlatformJwtService.TOKEN_TYPE)
                .claim("email", "platform@freightflow.com")
                .claim("platformRole", "PLATFORM_ADMIN")
                .claim("status", "ACTIVE")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor("another-platform-secret-minimum-32-bytes!!".getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertThat(service.validateAndGetClaims(wrongIssuer)).isNull();
        assertThat(service.validateAndGetClaims(wrongAudience)).isNull();
        assertThat(service.validateAndGetClaims(wrongSignature)).isNull();
    }

    @Test
    @DisplayName("rejeitaAudienceAusenteTokenTypeAusenteOuTenantEClaimsInvalidos")
    void rejeitaAudienceAusenteTokenTypeAusenteOuTenantEClaimsInvalidos() {
        PlatformJwtProperties properties = properties(
                "platform-test-secret-key-minimum-32-bytes-long!!",
                "freightflow-platform-test",
                "freightflow-platform-api-test",
                60_000
        );
        PlatformJwtService service = new PlatformJwtService(properties);
        Instant now = Instant.now();
        var key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));

        String missingAudience = Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .issuer(properties.getIssuer())
                .claim("token_type", PlatformJwtService.TOKEN_TYPE)
                .claim("email", "platform@freightflow.com")
                .claim("platformRole", "PLATFORM_ADMIN")
                .claim("status", "ACTIVE")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(60)))
                .signWith(key)
                .compact();

        String missingTokenType = Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .issuer(properties.getIssuer())
                .audience().add(properties.getAudience()).and()
                .claim("email", "platform@freightflow.com")
                .claim("platformRole", "PLATFORM_ADMIN")
                .claim("status", "ACTIVE")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(60)))
                .signWith(key)
                .compact();

        String tenantTokenType = Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .issuer(properties.getIssuer())
                .audience().add(properties.getAudience()).and()
                .claim("token_type", "TENANT")
                .claim("email", "platform@freightflow.com")
                .claim("platformRole", "PLATFORM_ADMIN")
                .claim("status", "ACTIVE")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(60)))
                .signWith(key)
                .compact();

        String invalidSubject = Jwts.builder()
                .subject("not-a-uuid")
                .issuer(properties.getIssuer())
                .audience().add(properties.getAudience()).and()
                .claim("token_type", PlatformJwtService.TOKEN_TYPE)
                .claim("email", "platform@freightflow.com")
                .claim("platformRole", "PLATFORM_ADMIN")
                .claim("status", "ACTIVE")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(60)))
                .signWith(key)
                .compact();

        String invalidRole = Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .issuer(properties.getIssuer())
                .audience().add(properties.getAudience()).and()
                .claim("token_type", PlatformJwtService.TOKEN_TYPE)
                .claim("email", "platform@freightflow.com")
                .claim("platformRole", "ADMIN")
                .claim("status", "ACTIVE")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(60)))
                .signWith(key)
                .compact();

        String withTenantClaims = Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .issuer(properties.getIssuer())
                .audience().add(properties.getAudience()).and()
                .claim("token_type", PlatformJwtService.TOKEN_TYPE)
                .claim("email", "platform@freightflow.com")
                .claim("platformRole", "PLATFORM_ADMIN")
                .claim("status", "ACTIVE")
                .claim("tenantId", UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(60)))
                .signWith(key)
                .compact();

        assertThat(service.validateAndGetClaims(missingAudience)).isNull();
        assertThat(service.validateAndGetClaims(missingTokenType)).isNull();
        assertThat(service.validateAndGetClaims(tenantTokenType)).isNull();
        assertThat(service.getPrincipalFromClaims(service.validateAndGetClaims(invalidSubject))).isNull();
        assertThat(service.getPrincipalFromClaims(service.validateAndGetClaims(invalidRole))).isNull();
        assertThat(service.getPrincipalFromClaims(service.validateAndGetClaims(withTenantClaims))).isNull();
    }

    private static PlatformJwtProperties properties(String secret, String issuer, String audience, long expirationMs) {
        PlatformJwtProperties properties = new PlatformJwtProperties();
        properties.setSecret(secret);
        properties.setIssuer(issuer);
        properties.setAudience(audience);
        properties.setExpirationMs(expirationMs);
        return properties;
    }
}
