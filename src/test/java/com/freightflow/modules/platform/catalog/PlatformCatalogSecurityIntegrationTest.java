package com.freightflow.modules.platform.catalog;

import com.freightflow.AbstractIntegrationTest;
import com.freightflow.config.PlatformJwtProperties;
import com.freightflow.modules.auth.Tenant;
import com.freightflow.modules.auth.TenantRepository;
import com.freightflow.modules.auth.User;
import com.freightflow.modules.auth.UserRepository;
import com.freightflow.modules.customer.Customer;
import com.freightflow.modules.customer.CustomerRepository;
import com.freightflow.modules.platform.PlatformRole;
import com.freightflow.modules.platform.PlatformUser;
import com.freightflow.modules.platform.PlatformUserRepository;
import com.freightflow.modules.platform.PlatformUserStatus;
import com.freightflow.shared.security.platform.PlatformJwtService;
import com.freightflow.shared.security.platform.PlatformPrincipal;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static com.jayway.jsonpath.JsonPath.read;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Platform catalog security integration")
class PlatformCatalogSecurityIntegrationTest extends AbstractIntegrationTest {

    @Autowired private PlatformUserRepository platformUserRepository;
    @Autowired private PlatformJwtService platformJwtService;
    @Autowired private PlatformJwtProperties platformJwtProperties;
    @Autowired private TenantRepository tenantRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("platformAdminAcessaCatalogoReadOnly")
    void platformAdminAcessaCatalogoReadOnly() throws Exception {
        platformUserRepository.saveAndFlush(new PlatformUser(
                "platform@freightflow.com",
                passwordEncoder.encode("Platform123"),
                PlatformRole.PLATFORM_ADMIN,
                PlatformUserStatus.ACTIVE
        ));

        String token = platformLogin("platform@freightflow.com", "Platform123");

        mockMvc.perform(get("/api/v1/platform/features")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].key").exists());

        mockMvc.perform(get("/api/v1/platform/features/tracking")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value("TRACKING"));

        mockMvc.perform(get("/api/v1/platform/plans")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].code").exists());

        mockMvc.perform(get("/api/v1/platform/plans/code/enterprise")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ENTERPRISE"))
                .andExpect(jsonPath("$.entitlements[0].featureKey").exists());
    }

    @Test
    @DisplayName("tokenTenantESemTokenSaoNegados")
    void tokenTenantESemTokenSaoNegados() throws Exception {
        seedOperationalUsers();
        String tenantToken = login("admin@tenant.com", "Tenant1234");

        mockMvc.perform(get("/api/v1/platform/features")
                        .header("Authorization", "Bearer " + tenantToken))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/platform/plans"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("entradasInvalidasRetornamBadRequestSeguro")
    void entradasInvalidasRetornamBadRequestSeguro() throws Exception {
        platformUserRepository.saveAndFlush(new PlatformUser(
                "platform@freightflow.com",
                passwordEncoder.encode("Platform123"),
                PlatformRole.PLATFORM_ADMIN,
                PlatformUserStatus.ACTIVE
        ));
        String token = platformLogin("platform@freightflow.com", "Platform123");

        mockMvc.perform(get("/api/v1/platform/features")
                        .param("valueType", "NOT_A_TYPE")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"));

        mockMvc.perform(get("/api/v1/platform/plans")
                        .param("status", "NOT_A_STATUS")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"));

        mockMvc.perform(get("/api/v1/platform/plans/not-a-uuid")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"));

        mockMvc.perform(get("/api/v1/platform/features/%20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/platform/plans/code/%20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/platform/features")
                        .param("page", "-1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Parameter 'page' must be greater than or equal to 0."));

        mockMvc.perform(get("/api/v1/platform/plans")
                        .param("size", "0")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Parameter 'size' must be between 1 and 100."));

        mockMvc.perform(get("/api/v1/platform/plans")
                        .param("size", "101")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Parameter 'size' must be between 1 and 100."));
    }

    @Test
    @DisplayName("naoEncontradosRetornam404Seguro")
    void naoEncontradosRetornam404Seguro() throws Exception {
        platformUserRepository.saveAndFlush(new PlatformUser(
                "platform@freightflow.com",
                passwordEncoder.encode("Platform123"),
                PlatformRole.PLATFORM_ADMIN,
                PlatformUserStatus.ACTIVE
        ));
        String token = platformLogin("platform@freightflow.com", "Platform123");

        mockMvc.perform(get("/api/v1/platform/features/UNKNOWN_FEATURE")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"));

        mockMvc.perform(get("/api/v1/platform/plans/code/UNKNOWN_PLAN")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"));
    }

    @Test
    @DisplayName("usuarioPlatformDesabilitadoOuExpiradoEhNegado")
    void usuarioPlatformDesabilitadoOuExpiradoEhNegado() throws Exception {
        PlatformUser user = platformUserRepository.saveAndFlush(new PlatformUser(
                "platform@freightflow.com",
                passwordEncoder.encode("Platform123"),
                PlatformRole.PLATFORM_ADMIN,
                PlatformUserStatus.ACTIVE
        ));
        String token = platformLogin("platform@freightflow.com", "Platform123");

        user.setStatus(PlatformUserStatus.DISABLED);
        platformUserRepository.saveAndFlush(user);

        mockMvc.perform(get("/api/v1/platform/features")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());

        String expiredToken = Jwts.builder()
                .subject(user.getId().toString())
                .issuer(platformJwtProperties.getIssuer())
                .audience().add(platformJwtProperties.getAudience()).and()
                .claim("token_type", PlatformJwtService.TOKEN_TYPE)
                .claim("email", user.getEmail())
                .claim("platformRole", user.getRole().name())
                .claim("status", PlatformUserStatus.ACTIVE.name())
                .issuedAt(Date.from(Instant.now().minusSeconds(120)))
                .expiration(Date.from(Instant.now().minusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(platformJwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)))
                .compact();

        mockMvc.perform(get("/api/v1/platform/plans")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("tokenPlatformNaoAcessaNamespaceTenantEMetodosMutaveisNaoExecutam")
    void tokenPlatformNaoAcessaNamespaceTenantEMetodosMutaveisNaoExecutam() throws Exception {
        platformUserRepository.saveAndFlush(new PlatformUser(
                "platform@freightflow.com",
                passwordEncoder.encode("Platform123"),
                PlatformRole.PLATFORM_ADMIN,
                PlatformUserStatus.ACTIVE
        ));
        String token = platformLogin("platform@freightflow.com", "Platform123");

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/platform/features")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isMethodNotAllowed());

        String starterPlanId = "0fbe6f06-6416-4c8a-9382-6a5ff1a4a101";
        mockMvc.perform(delete("/api/v1/platform/plans/{id}", starterPlanId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isMethodNotAllowed());
    }

    private void seedOperationalUsers() {
        Tenant tenant = tenantRepository.saveAndFlush(
                new Tenant("Tenant One", "tenant-one", "admin@tenant.com", "FREE")
        );

        User admin = new User(
                "Tenant Admin",
                "admin@tenant.com",
                passwordEncoder.encode("Tenant1234"),
                User.UserRole.ADMIN,
                tenant
        );
        userRepository.saveAndFlush(admin);

        Customer customer = customerRepository.saveAndFlush(new Customer(tenant, "Atlas Cargo"));
        User client = new User(
                "Tenant Client",
                "client@tenant.com",
                passwordEncoder.encode("Tenant1234"),
                User.UserRole.CLIENT,
                tenant
        );
        client.setCustomer(customer);
        userRepository.saveAndFlush(client);
    }

    private String platformLogin(String email, String password) throws Exception {
        String responseBody = mockMvc.perform(post("/api/v1/platform/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = read(responseBody, "$.accessToken");
        assertThat(token).isNotBlank();
        return token;
    }
}
