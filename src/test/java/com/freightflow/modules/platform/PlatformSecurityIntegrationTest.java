package com.freightflow.modules.platform;

import com.jayway.jsonpath.JsonPath;
import com.freightflow.AbstractIntegrationTest;
import com.freightflow.modules.auth.Tenant;
import com.freightflow.modules.auth.TenantRepository;
import com.freightflow.modules.auth.User;
import com.freightflow.modules.auth.UserRepository;
import com.freightflow.modules.customer.Customer;
import com.freightflow.modules.customer.CustomerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Platform security integration")
class PlatformSecurityIntegrationTest extends AbstractIntegrationTest {

    @Autowired private PlatformUserRepository platformUserRepository;
    @Autowired private TenantRepository tenantRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("loginPlatformValidoEMeNaoExibePasswordHash")
    void loginPlatformValidoEMeNaoExibePasswordHash() throws Exception {
        platformUserRepository.saveAndFlush(new PlatformUser(
                "platform@freightflow.com",
                passwordEncoder.encode("Platform123"),
                PlatformRole.PLATFORM_ADMIN,
                PlatformUserStatus.ACTIVE
        ));

        String token = platformLogin("platform@freightflow.com", "Platform123");

        mockMvc.perform(get("/api/v1/platform/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("platform@freightflow.com"))
                .andExpect(jsonPath("$.platformRole").value("PLATFORM_ADMIN"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.tenantId").doesNotExist())
                .andExpect(jsonPath("$.customerId").doesNotExist());
    }

    @Test
    @DisplayName("loginPlatformInvalidoRetornaRespostaSegura")
    void loginPlatformInvalidoRetornaRespostaSegura() throws Exception {
        platformUserRepository.saveAndFlush(new PlatformUser(
                "platform@freightflow.com",
                passwordEncoder.encode("Platform123"),
                PlatformRole.PLATFORM_ADMIN,
                PlatformUserStatus.ACTIVE
        ));

        mockMvc.perform(post("/api/v1/platform/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"platform@freightflow.com","password":"WrongPass123"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid email or password"));
    }

    @Test
    @DisplayName("tokenOperacionalNaoAcessaPlatformETokenPlatformNaoAcessaOperacional")
    void tokenOperacionalNaoAcessaPlatformETokenPlatformNaoAcessaOperacional() throws Exception {
        platformUserRepository.saveAndFlush(new PlatformUser(
                "platform@freightflow.com",
                passwordEncoder.encode("Platform123"),
                PlatformRole.PLATFORM_ADMIN,
                PlatformUserStatus.ACTIVE
        ));
        seedOperationalUsers();

        String platformToken = platformLogin("platform@freightflow.com", "Platform123");
        String tenantToken = login("admin@tenant.com", "Tenant1234");

        mockMvc.perform(get("/api/v1/platform/me")
                        .header("Authorization", "Bearer " + tenantToken))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + platformToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("tokenEmitidoEhInvalidadoQuandoContaEhDesabilitadaOuRemovida")
    void tokenEmitidoEhInvalidadoQuandoContaEhDesabilitadaOuRemovida() throws Exception {
        PlatformUser user = platformUserRepository.saveAndFlush(new PlatformUser(
                "platform@freightflow.com",
                passwordEncoder.encode("Platform123"),
                PlatformRole.PLATFORM_ADMIN,
                PlatformUserStatus.ACTIVE
        ));

        String token = platformLogin("platform@freightflow.com", "Platform123");

        user.setStatus(PlatformUserStatus.DISABLED);
        platformUserRepository.saveAndFlush(user);

        mockMvc.perform(get("/api/v1/platform/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());

        platformUserRepository.delete(user);
        mockMvc.perform(get("/api/v1/platform/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("rotaPlatformInexistenteEhProtegidaEHttpMethodInvalidoNaoFicaPublico")
    void rotaPlatformInexistenteEhProtegidaEHttpMethodInvalidoNaoFicaPublico() throws Exception {
        mockMvc.perform(get("/api/v1/platform/unknown"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/platform/auth/login"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("adminClientESemTokenNaoAcessamPlatformMasLoginOperacionalContinua")
    void adminClientESemTokenNaoAcessamPlatformMasLoginOperacionalContinua() throws Exception {
        seedOperationalUsers();

        String adminToken = login("admin@tenant.com", "Tenant1234");
        String clientToken = login("client@tenant.com", "Tenant1234");

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@tenant.com"));

        mockMvc.perform(get("/api/v1/platform/me")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/platform/me")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/platform/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("platformUserDesabilitadoNaoConsegueLogar")
    void platformUserDesabilitadoNaoConsegueLogar() throws Exception {
        platformUserRepository.saveAndFlush(new PlatformUser(
                "disabled@freightflow.com",
                passwordEncoder.encode("Platform123"),
                PlatformRole.PLATFORM_ADMIN,
                PlatformUserStatus.DISABLED
        ));

        mockMvc.perform(post("/api/v1/platform/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"disabled@freightflow.com","password":"Platform123"}
                                """))
                .andExpect(status().isUnauthorized());
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

        String token = JsonPath.read(responseBody, "$.accessToken");
        assertThat(token).isNotBlank();
        return token;
    }
}
