package com.freightflow.modules.platform.subscription;

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

@DisplayName("Platform tenant subscription security integration")
class PlatformTenantSubscriptionSecurityIntegrationTest extends AbstractIntegrationTest {

    @Autowired private PlatformUserRepository platformUserRepository;
    @Autowired private TenantRepository tenantRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("get current e assign funcionam com platform token")
    void getCurrentEAssignFuncionamComPlatformToken() throws Exception {
        platformUserRepository.saveAndFlush(new PlatformUser(
                "platform@freightflow.com",
                passwordEncoder.encode("Platform123"),
                PlatformRole.PLATFORM_ADMIN,
                PlatformUserStatus.ACTIVE
        ));
        Tenant tenant = tenantRepository.saveAndFlush(new Tenant("Tenant One", "tenant-one", "ops@tenant.com", "LEGACY"));
        String token = platformLogin("platform@freightflow.com", "Platform123");

        mockMvc.perform(get("/api/v1/platform/tenants/{tenantId}/subscription", tenant.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value(tenant.getId().toString()))
                .andExpect(jsonPath("$.currentSubscription").doesNotExist());

        mockMvc.perform(post("/api/v1/platform/tenants/{tenantId}/subscription/assign", tenant.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"planCode":"professional","reason":"Initial assignment"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plan.code").value("PROFESSIONAL"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mockMvc.perform(get("/api/v1/platform/tenants/{tenantId}/subscription", tenant.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentSubscription.plan.code").value("PROFESSIONAL"));
    }

    @Test
    @DisplayName("token tenant e sem token nao acessam")
    void tokenTenantESemTokenNaoAcessam() throws Exception {
        seedOperationalUsers();
        String tenantToken = login("admin@tenant.com", "Tenant1234");

        mockMvc.perform(get("/api/v1/platform/tenants/{tenantId}/subscription", UUID.randomUUID())
                        .header("Authorization", "Bearer " + tenantToken))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/platform/tenants/{tenantId}/subscription", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("erros seguros para tenant id malformado plan code vazio e plano inexistente")
    void errosSegurosParaTenantIdMalformadoPlanCodeVazioEPlanoInexistente() throws Exception {
        platformUserRepository.saveAndFlush(new PlatformUser(
                "platform@freightflow.com",
                passwordEncoder.encode("Platform123"),
                PlatformRole.PLATFORM_ADMIN,
                PlatformUserStatus.ACTIVE
        ));
        Tenant tenant = tenantRepository.saveAndFlush(new Tenant("Tenant One", "tenant-one", "ops@tenant.com", "LEGACY"));
        String token = platformLogin("platform@freightflow.com", "Platform123");

        mockMvc.perform(get("/api/v1/platform/tenants/not-a-uuid/subscription")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"));

        mockMvc.perform(post("/api/v1/platform/tenants/{tenantId}/subscription/assign", tenant.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"planCode":"   "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"));

        MvcResult missingPlanResult = mockMvc.perform(post("/api/v1/platform/tenants/{tenantId}/subscription/assign", tenant.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"planCode":"unknown"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"))
                .andExpect(jsonPath("$.detail").value("Subscription plan not found: UNKNOWN"))
                .andReturn();

        String body = missingPlanResult.getResponse().getContentAsString();
        assertThat(body).doesNotContain("constraint");
        assertThat(body).doesNotContain("select");
        assertThat(body).doesNotContain("Authorization");
    }

    private void seedOperationalUsers() {
        Tenant tenant = tenantRepository.saveAndFlush(
                new Tenant("Tenant Ops", "tenant-ops", "admin@tenant.com", "FREE")
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
