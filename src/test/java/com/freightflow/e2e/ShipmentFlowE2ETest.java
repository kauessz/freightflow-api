package com.freightflow.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightflow.helpers.DatabaseCleaner;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.*;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Fluxo E2E de embarque marítimo — 6 passos ordenados em uma única suite.
 *
 * <p>Não estende {@code AbstractIntegrationTest} intencionalmente: o
 * {@code @BeforeEach resetDatabase()} da classe base limparia o banco entre
 * cada step, quebrando o estado acumulado do fluxo.</p>
 *
 * <p>Aqui o banco é limpo apenas uma vez, no {@code @BeforeAll}, e o estado
 * (token, shipmentId) é compartilhado via campos estáticos.</p>
 */
@Disabled("Requires Docker environment — runs on CI via GitHub Actions")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Shipment Flow — E2E")
class ShipmentFlowE2ETest {

    // ── Container dedicado a este teste ───────────────────────────────────

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("freightflow_e2e")
                    .withUsername("freight")
                    .withPassword("freight123")
                    .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    // ── Injeções ──────────────────────────────────────────────────────────

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired DatabaseCleaner databaseCleaner;

    // ── Estado compartilhado entre steps ──────────────────────────────────

    private static final String EMAIL    = "e2e-operator@freightflow.io";
    private static final String PASSWORD = "SecurePass123";
    private static final String BOOKING  = "P10E2E001";

    /** Voyage pré-semeada pelo V11 (MSC-2026-001 — Santos → Rotterdam). */
    private static final String VOYAGE_ID = "b0000001-0000-0000-0000-000000000001";

    private static String accessToken;
    private static String shipmentId;

    // ── Setup: limpa o banco UMA VEZ antes de todos os steps ─────────────

    @BeforeAll
    static void setupOnce() {
        // Campos estáticos são resetados antes da suite para evitar
        // contaminação caso o mesmo container seja reutilizado (TC_REUSABLE=true)
        accessToken = null;
        shipmentId  = null;
    }

    // ── Steps ─────────────────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("step1 — registra operador e espera 201")
    void step1_registerOperator() throws Exception {
        // Garante banco limpo no início do fluxo (apenas aqui)
        databaseCleaner.clean();

        String body = """
                {
                  "name": "E2E Operator",
                  "email": "%s",
                  "password": "%s",
                  "companyName": "E2E Freight Co."
                }
                """.formatted(EMAIL, PASSWORD);

        String response = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        accessToken = JsonPath.read(response, "$.accessToken");
        assertThat(accessToken).isNotBlank();
    }

    @Test
    @Order(2)
    @DisplayName("step2 — login retorna access token JWT")
    void step2_login() throws Exception {
        String body = """
                {"email": "%s", "password": "%s"}
                """.formatted(EMAIL, PASSWORD);

        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        accessToken = JsonPath.read(response, "$.accessToken");
        assertThat(accessToken).isNotBlank();
    }

    @Test
    @Order(3)
    @DisplayName("step3 — cria embarque P10E2E001 via voyage MSC-2026-001")
    void step3_createShipment() throws Exception {
        // Resolve IDs dos portos a partir do UNLOCODE (semeados pelo V10)
        String santosResponse = mockMvc.perform(get("/api/v1/ports/unlocode/BRSSZ")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String santosId = JsonPath.read(santosResponse, "$.id");

        String rotterdamResponse = mockMvc.perform(get("/api/v1/ports/unlocode/NLRTM")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String rotterdamId = JsonPath.read(rotterdamResponse, "$.id");

        String body = """
                {
                  "booking": "%s",
                  "containerNumber": "MSCU1234567",
                  "containerType": "TEU40",
                  "voyageId": "%s",
                  "originPortId": "%s",
                  "destinationPortId": "%s",
                  "shipper": "Brazil Exports Ltda",
                  "consignee": "European Imports BV"
                }
                """.formatted(BOOKING, VOYAGE_ID, santosId, rotterdamId);

        String response = mockMvc.perform(post("/api/v1/shipments")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.booking").value(BOOKING))
                .andExpect(jsonPath("$.status").value("BOOKED"))
                .andReturn().getResponse().getContentAsString();

        shipmentId = JsonPath.read(response, "$.id");
        assertThat(shipmentId).isNotBlank();
    }

    @Test
    @Order(4)
    @DisplayName("step4 — registra evento GATE_IN para o embarque")
    void step4_registerGateInEvent() throws Exception {
        String occurredAt = Instant.now().truncatedTo(ChronoUnit.SECONDS).toString();
        String body = """
                {
                  "type": "GATE_IN",
                  "location": "Terminal de Contêineres de Santos, BR",
                  "description": "Container entrou no pátio do terminal",
                  "occurredAt": "%s"
                }
                """.formatted(occurredAt);

        mockMvc.perform(post("/api/v1/shipments/{id}/events", shipmentId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("GATE_IN"))
                .andExpect(jsonPath("$.shipmentId").value(shipmentId));
    }

    @Test
    @Order(5)
    @DisplayName("step5 — status do embarque deve ser GATE_IN após o evento")
    void step5_verifyStatusUpdated() throws Exception {
        mockMvc.perform(get("/api/v1/shipments/{id}", shipmentId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(shipmentId))
                .andExpect(jsonPath("$.status").value("GATE_IN"));
    }

    @Test
    @Order(6)
    @DisplayName("step6 — rastreamento público retorna 200 sem autenticação")
    void step6_publicTrackingWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/tracking/{booking}", BOOKING))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.booking").value(BOOKING))
                .andExpect(jsonPath("$.status").isNotEmpty());
    }
}
