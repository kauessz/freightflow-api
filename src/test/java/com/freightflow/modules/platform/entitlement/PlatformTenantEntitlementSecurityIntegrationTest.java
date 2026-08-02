package com.freightflow.modules.platform.entitlement;

import com.freightflow.AbstractIntegrationTest;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static com.jayway.jsonpath.JsonPath.read;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Platform tenant entitlement security integration")
class PlatformTenantEntitlementSecurityIntegrationTest extends AbstractIntegrationTest {

    @Autowired private PlatformUserRepository platformUserRepository;
    @Autowired private TenantRepository tenantRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("platform token acessa entitlements e tenant inexistente retorna 404")
    void platformTokenAcessaEntitlementsETenantInexistenteRetorna404() throws Exception {
        platformUserRepository.saveAndFlush(new PlatformUser(
                "platform@freightflow.com",
                passwordEncoder.encode("Platform123"),
                PlatformRole.PLATFORM_ADMIN,
                PlatformUserStatus.ACTIVE
        ));
        Tenant tenant = tenantRepository.saveAndFlush(new Tenant("Tenant One", "tenant-one", "ops@tenant.com", "LEGACY"));
        String token = platformLogin("platform@freightflow.com", "Platform123");

        mockMvc.perform(get("/api/v1/platform/tenants/{tenantId}/entitlements", tenant.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value(tenant.getId().toString()))
                .andExpect(jsonPath("$.accessStatus").value("NO_SUBSCRIPTION"))
                .andExpect(jsonPath("$.subscription").doesNotExist())
                .andExpect(jsonPath("$.features[0].featureKey").exists());

        MvcResult missingTenant = mockMvc.perform(get("/api/v1/platform/tenants/{tenantId}/entitlements", UUID.randomUUID())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"))
                .andReturn();

        String response = missingTenant.getResponse().getContentAsString();
        assertThat(response).doesNotContain("stack");
        assertThat(response).doesNotContain("Authorization");
        assertThat(response).doesNotContain("constraint");
    }

    @Test
    @DisplayName("token tenant e sem token sao rejeitados e uuid malformado retorna 400")
    void tokenTenantESemTokenSaoRejeitadosEUuidMalformadoRetorna400() throws Exception {
        seedOperationalUsers();
        String tenantToken = login("admin@tenant.com", "Tenant1234");

        mockMvc.perform(get("/api/v1/platform/tenants/{tenantId}/entitlements", UUID.randomUUID())
                        .header("Authorization", "Bearer " + tenantToken))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/platform/tenants/{tenantId}/entitlements", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());

        platformUserRepository.saveAndFlush(new PlatformUser(
                "platform@freightflow.com",
                passwordEncoder.encode("Platform123"),
                PlatformRole.PLATFORM_ADMIN,
                PlatformUserStatus.ACTIVE
        ));
        String platformToken = platformLogin("platform@freightflow.com", "Platform123");

        mockMvc.perform(get("/api/v1/platform/tenants/not-a-uuid/entitlements")
                        .header("Authorization", "Bearer " + platformToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"));
    }

    @Test
    @DisplayName("platform user desabilitado apos emissao do token recebe 401")
    void platformUserDesabilitadoAposEmissaoDoTokenRecebe401() throws Exception {
        PlatformUser user = platformUserRepository.saveAndFlush(new PlatformUser(
                "platform@freightflow.com",
                passwordEncoder.encode("Platform123"),
                PlatformRole.PLATFORM_ADMIN,
                PlatformUserStatus.ACTIVE
        ));
        Tenant tenant = tenantRepository.saveAndFlush(new Tenant("Tenant Disabled", "tenant-disabled", "ops5@tenant.com", "LEGACY"));
        String token = platformLogin("platform@freightflow.com", "Platform123");

        user.setStatus(PlatformUserStatus.DISABLED);
        platformUserRepository.saveAndFlush(user);

        mockMvc.perform(get("/api/v1/platform/tenants/{tenantId}/entitlements", tenant.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    private void seedOperationalUsers() {
        Tenant tenant = tenantRepository.saveAndFlush(
                new Tenant("Tenant Ops", "tenant-ops", "admin@tenant.com", "FREE")
        );

        userRepository.saveAndFlush(new User(
                "Tenant Admin",
                "admin@tenant.com",
                passwordEncoder.encode("Tenant1234"),
                User.UserRole.ADMIN,
                tenant
        ));

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
