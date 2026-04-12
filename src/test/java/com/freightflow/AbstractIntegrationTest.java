package com.freightflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightflow.helpers.DatabaseCleaner;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Classe base para todos os testes de integração.
 *
 * <p>Inicia um PostgreSQL 16 via Testcontainers uma única vez por suite
 * (container estático = reutilizado entre todas as subclasses).
 * O {@link DatabaseCleaner} trunca as tabelas antes de cada teste,
 * garantindo isolamento sem recriar o schema.</p>
 *
 * <p>Redis e RabbitMQ são desabilitados via application-test.yml.</p>
 *
 * <p>Uso:
 * <pre>{@code
 * class MeuTest extends AbstractIntegrationTest {
 *
 *     @Test
 *     void meuTeste() throws Exception {
 *         mockMvc.perform(get("/api/v1/health")).andExpect(status().isOk());
 *     }
 * }
 * }</pre>
 * </p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
public abstract class AbstractIntegrationTest {

    // ── Container único e reutilizável entre todos os testes ──────────────
    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("freightflow_test")
                    .withUsername("freight")
                    .withPassword("freight123")
                    .withReuse(true);   // reusa o container entre execuções com TC_REUSABLE=true

    /**
     * Sobrescreve as propriedades de datasource do application-test.yml
     * com os valores reais do container iniciado pelo Testcontainers.
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    // ── Beans injetados ───────────────────────────────────────────────────

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    // ── Ciclo de vida ─────────────────────────────────────────────────────

    /**
     * Limpa todas as tabelas antes de cada teste.
     * Garante isolamento completo entre testes sem recriar o schema.
     */
    @BeforeEach
    void resetDatabase() {
        databaseCleaner.clean();
    }

    // ── Helpers para obter token JWT ──────────────────────────────────────

    /**
     * Registra um usuário e retorna o access token JWT.
     * Conveniente para testes que precisam de autenticação.
     *
     * @param name        nome do usuário
     * @param email       e-mail
     * @param password    senha (mín. 8 chars)
     * @param companyName nome da empresa (cria um novo tenant)
     * @return JWT access token como String
     */
    protected String registerAndLogin(String name, String email, String password, String companyName)
            throws Exception {
        String registerJson = objectMapper.writeValueAsString(
                new com.freightflow.modules.auth.dto.RegisterRequest(name, email, password, companyName)
        );

        String responseBody = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/v1/auth/register")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return com.jayway.jsonpath.JsonPath.read(responseBody, "$.accessToken");
    }

    /**
     * Faz login e retorna o access token JWT.
     * Assume que o usuário já foi registrado.
     */
    protected String login(String email, String password) throws Exception {
        String loginJson = String.format(
                "{\"email\":\"%s\",\"password\":\"%s\"}", email, password);

        String responseBody = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/v1/auth/login")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return com.jayway.jsonpath.JsonPath.read(responseBody, "$.accessToken");
    }
}
