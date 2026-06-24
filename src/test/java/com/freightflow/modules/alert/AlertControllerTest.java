package com.freightflow.modules.alert;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightflow.config.TestSecurityConfig;
import com.freightflow.fixtures.TestDataFactory;
import com.freightflow.modules.alert.dto.AlertResponse;
import com.freightflow.modules.alert.dto.CreateAlertRequest;
import com.freightflow.modules.alert.enums.AlertType;
import com.freightflow.modules.alert.enums.Severity;
import com.freightflow.shared.exception.GlobalExceptionHandler;
import com.freightflow.shared.exception.ResourceNotFoundException;
import com.freightflow.shared.security.UserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AlertController.class)
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = true)
@DisplayName("AlertController")
class AlertControllerTest {

    @Autowired private MockMvc       mockMvc;
    @Autowired private ObjectMapper  objectMapper;

    @MockBean  private AlertService  alertService;

    private final UserPrincipal principal = TestDataFactory.principal();

    // ── helpers ───────────────────────────────────────────────────────────────

    private AlertResponse sampleAlert() {
        return new AlertResponse(
                UUID.randomUUID(),
                TestDataFactory.defaultShipmentId(),
                "A123456789",
                AlertType.DELAY,
                Severity.HIGH,
                "Vessel delayed by 24 hours",
                false,
                null,
                Instant.now()
        );
    }

    // ==================== GET /api/v1/alerts ====================

    @Nested
    @DisplayName("GET /api/v1/alerts")
    class ListOpenAlerts {

        @Test
        @DisplayName("Deve retornar 200 com lista de alerts em aberto do tenant")
        void deveRetornar200ComLista() throws Exception {
            when(alertService.findOpenByTenant(any(UUID.class)))
                    .thenReturn(List.of(sampleAlert()));

            mockMvc.perform(get("/api/v1/alerts")
                            .with(user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].booking").value("A123456789"))
                    .andExpect(jsonPath("$[0].type").value("DELAY"))
                    .andExpect(jsonPath("$[0].severity").value("HIGH"))
                    .andExpect(jsonPath("$[0].resolved").value(false));
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando nao ha alerts")
        void deveRetornar200ListaVazia() throws Exception {
            when(alertService.findOpenByTenant(any(UUID.class)))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/v1/alerts")
                            .with(user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("Deve retornar 401 sem autenticacao")
        void deveRetornar401SemAuth() throws Exception {
            mockMvc.perform(get("/api/v1/alerts"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== GET /api/v1/shipments/{id}/alerts ====================

    @Nested
    @DisplayName("GET /api/v1/shipments/{shipmentId}/alerts")
    class ListAlertsByShipment {

        @Test
        @DisplayName("Deve retornar 200 com lista de alerts do shipment")
        void deveRetornar200ComLista() throws Exception {
            UUID shipmentId = TestDataFactory.defaultShipmentId();
            when(alertService.findByShipment(shipmentId))
                    .thenReturn(List.of(sampleAlert()));

            mockMvc.perform(get("/api/v1/shipments/{shipmentId}/alerts", shipmentId)
                            .with(user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].shipmentId").value(shipmentId.toString()));
        }

        @Test
        @DisplayName("Deve retornar 404 quando shipment inexistente")
        void deveRetornar404ShipmentInexistente() throws Exception {
            UUID unknownId = UUID.randomUUID();
            when(alertService.findByShipment(unknownId))
                    .thenThrow(new ResourceNotFoundException("Shipment", unknownId));

            mockMvc.perform(get("/api/v1/shipments/{shipmentId}/alerts", unknownId)
                            .with(user(principal)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Resource Not Found"));
        }
    }

    // ==================== POST /api/v1/alerts ====================

    @Nested
    @DisplayName("POST /api/v1/alerts")
    class CreateAlert {

        @Test
        @DisplayName("Deve retornar 201 ao criar alert valido")
        void deveRetornar201() throws Exception {
            CreateAlertRequest request = new CreateAlertRequest(
                    TestDataFactory.defaultShipmentId(),
                    AlertType.DELAY,
                    Severity.HIGH,
                    "Vessel delayed due to port congestion"
            );

            when(alertService.create(any(CreateAlertRequest.class), any(UUID.class)))
                    .thenReturn(sampleAlert());

            mockMvc.perform(post("/api/v1/alerts")
                            .with(csrf())
                            .with(user(principal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.type").value("DELAY"))
                    .andExpect(jsonPath("$.severity").value("HIGH"))
                    .andExpect(jsonPath("$.resolved").value(false));
        }

        @Test
        @DisplayName("Deve retornar 400 com body invalido — shipmentId faltando")
        void deveRetornar400BodyInvalido() throws Exception {
            // Missing shipmentId (required) and message (required)
            String invalidJson = "{\"type\":\"DELAY\",\"severity\":\"HIGH\"}";

            mockMvc.perform(post("/api/v1/alerts")
                            .with(csrf())
                            .with(user(principal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar 400 com message em branco")
        void deveRetornar400MensagemEmBranco() throws Exception {
            String json = objectMapper.writeValueAsString(new CreateAlertRequest(
                    TestDataFactory.defaultShipmentId(),
                    AlertType.DELAY,
                    Severity.MEDIUM,
                    ""   // blank — @NotBlank should reject
            ));

            mockMvc.perform(post("/api/v1/alerts")
                            .with(csrf())
                            .with(user(principal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());
        }
    }

    // ==================== POST /api/v1/alerts/{id}/resolve ====================

    @Nested
    @DisplayName("POST /api/v1/alerts/{id}/resolve")
    class ResolveAlert {

        @Test
        @DisplayName("Deve retornar 200 com alert resolvido")
        void deveRetornar200AlertResolvido() throws Exception {
            UUID alertId = UUID.randomUUID();
            AlertResponse resolved = new AlertResponse(
                    alertId,
                    TestDataFactory.defaultShipmentId(),
                    "A123456789",
                    AlertType.DELAY,
                    Severity.HIGH,
                    "Vessel delayed by 24 hours",
                    true,
                    Instant.now(),
                    Instant.now().minusSeconds(3600)
            );

            when(alertService.resolve(eq(alertId), any(UUID.class)))
                    .thenReturn(resolved);

            mockMvc.perform(post("/api/v1/alerts/{id}/resolve", alertId)
                            .with(csrf())
                            .with(user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resolved").value(true))
                    .andExpect(jsonPath("$.resolvedAt").isNotEmpty());
        }

        @Test
        @DisplayName("Deve retornar 404 quando alert nao existe ou pertence a outro tenant")
        void deveRetornar404AlertInexistente() throws Exception {
            UUID unknownId = UUID.randomUUID();
            when(alertService.resolve(eq(unknownId), any(UUID.class)))
                    .thenThrow(new ResourceNotFoundException("Alert", unknownId));

            mockMvc.perform(post("/api/v1/alerts/{id}/resolve", unknownId)
                            .with(csrf())
                            .with(user(principal)))
                    .andExpect(status().isNotFound());
        }
    }
}
