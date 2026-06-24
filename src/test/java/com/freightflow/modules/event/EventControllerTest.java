package com.freightflow.modules.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightflow.config.TestSecurityConfig;
import com.freightflow.fixtures.TestDataFactory;
import com.freightflow.modules.event.dto.CreateEventRequest;
import com.freightflow.modules.event.dto.EventResponse;
import com.freightflow.modules.event.enums.EventType;
import com.freightflow.shared.exception.GlobalExceptionHandler;
import com.freightflow.shared.exception.ResourceNotFoundException;
import com.freightflow.shared.rbac.RoleCheckAspect;
import com.freightflow.shared.security.UserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EventController.class)
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class, EventControllerTest.RoleAspectTestConfig.class})
@AutoConfigureMockMvc(addFilters = true)
@DisplayName("EventController")
class EventControllerTest {

    @TestConfiguration
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    static class RoleAspectTestConfig {
        @Bean
        RoleCheckAspect roleCheckAspect() {
            return new RoleCheckAspect();
        }
    }

    @Autowired private MockMvc      mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean  private EventService eventService;

    private final UserPrincipal  adminPrincipal  = TestDataFactory.principal();
    private final UserPrincipal  viewerPrincipal = new UserPrincipal(
            UUID.fromString("bbbb0000-0000-0000-0000-000000000002"),
            "viewer@mercosul.com",
            null,
            TestDataFactory.defaultTenantId(),
            "VIEWER",
            null
    );
    private final UserPrincipal  clientPrincipal = new UserPrincipal(
            UUID.fromString("bbbb0000-0000-0000-0000-000000000003"),
            "client@mercosul.com",
            null,
            TestDataFactory.defaultTenantId(),
            "CLIENT",
            UUID.fromString("99990000-0000-0000-0000-000000000001")
    );
    private final UUID           shipmentId = TestDataFactory.defaultShipmentId();

    // ── helpers ───────────────────────────────────────────────────────────────

    private EventResponse sampleEvent(UUID eventId) {
        return new EventResponse(
                eventId,
                shipmentId,
                "A123456789",
                EventType.GATE_IN,
                "Santos, Brazil",
                "Container received at gate",
                Instant.now().minusSeconds(3600),
                Instant.now().minusSeconds(3500)
        );
    }

    // ==================== GET /api/v1/shipments/{shipmentId}/events ====================

    @Nested
    @DisplayName("GET /api/v1/shipments/{shipmentId}/events")
    class ListEvents {

        @Test
        @DisplayName("Deve retornar 200 com lista de eventos do shipment")
        void deveRetornar200ComLista() throws Exception {
            UUID eventId = UUID.randomUUID();
            when(eventService.listByShipment(shipmentId, TestDataFactory.defaultTenantId(), null))
                    .thenReturn(List.of(sampleEvent(eventId)));

            mockMvc.perform(get("/api/v1/shipments/{shipmentId}/events", shipmentId)
                            .with(user(adminPrincipal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].type").value("GATE_IN"))
                    .andExpect(jsonPath("$[0].shipmentId").value(shipmentId.toString()))
                    .andExpect(jsonPath("$[0].location").value("Santos, Brazil"));
        }

        @Test
        @DisplayName("Deve retornar 404 quando shipment inexistente")
        void deveRetornar404ShipmentInexistente() throws Exception {
            UUID unknownId = UUID.randomUUID();
            when(eventService.listByShipment(unknownId, TestDataFactory.defaultTenantId(), null))
                    .thenThrow(new ResourceNotFoundException("Shipment", unknownId));

            mockMvc.perform(get("/api/v1/shipments/{shipmentId}/events", unknownId)
                            .with(user(adminPrincipal)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Resource Not Found"));
        }

        @Test
        @DisplayName("CLIENT repassa customerId ao listar eventos")
        void clientRepassaCustomerIdAoListarEventos() throws Exception {
            UUID eventId = UUID.randomUUID();
            when(eventService.listByShipment(shipmentId, TestDataFactory.defaultTenantId(), clientPrincipal.getCustomerId()))
                    .thenReturn(List.of(sampleEvent(eventId)));

            mockMvc.perform(get("/api/v1/shipments/{shipmentId}/events", shipmentId)
                            .with(user(clientPrincipal)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Deve retornar 401 sem autenticacao")
        void deveRetornar401SemAuth() throws Exception {
            mockMvc.perform(get("/api/v1/shipments/{shipmentId}/events", shipmentId))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== GET /api/v1/shipments/{shipmentId}/events/{eventId} ====================

    @Nested
    @DisplayName("GET /api/v1/shipments/{shipmentId}/events/{eventId}")
    class GetEventById {

        @Test
        @DisplayName("Deve retornar 200 com evento encontrado")
        void deveRetornar200() throws Exception {
            UUID eventId = UUID.randomUUID();
            when(eventService.getById(shipmentId, eventId, TestDataFactory.defaultTenantId(), null))
                    .thenReturn(sampleEvent(eventId));

            mockMvc.perform(get("/api/v1/shipments/{shipmentId}/events/{eventId}",
                                    shipmentId, eventId)
                            .with(user(adminPrincipal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(eventId.toString()))
                    .andExpect(jsonPath("$.type").value("GATE_IN"))
                    .andExpect(jsonPath("$.booking").value("A123456789"));
        }

        @Test
        @DisplayName("Deve retornar 404 quando evento nao existe")
        void deveRetornar404EventoInexistente() throws Exception {
            UUID unknownEventId = UUID.randomUUID();
            when(eventService.getById(shipmentId, unknownEventId, TestDataFactory.defaultTenantId(), null))
                    .thenThrow(new ResourceNotFoundException("Event", unknownEventId));

            mockMvc.perform(get("/api/v1/shipments/{shipmentId}/events/{eventId}",
                                    shipmentId, unknownEventId)
                            .with(user(adminPrincipal)))
                    .andExpect(status().isNotFound());
        }
    }

    // ==================== POST /api/v1/shipments/{shipmentId}/events ====================

    @Nested
    @DisplayName("POST /api/v1/shipments/{shipmentId}/events")
    class CreateEvent {

        @Test
        @DisplayName("Deve retornar 201 ao criar evento valido")
        void deveRetornar201() throws Exception {
            UUID eventId = UUID.randomUUID();
            CreateEventRequest request = new CreateEventRequest(
                    EventType.GATE_IN,
                    "Santos, Brazil",
                    "Container received at gate",
                    Instant.now()
            );

            when(eventService.create(eq(shipmentId), any(CreateEventRequest.class), eq(TestDataFactory.defaultTenantId()), eq(null)))
                    .thenReturn(sampleEvent(eventId));

            mockMvc.perform(post("/api/v1/shipments/{shipmentId}/events", shipmentId)
                            .with(csrf())
                            .with(user(adminPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.type").value("GATE_IN"))
                    .andExpect(jsonPath("$.location").value("Santos, Brazil"));
        }

        @Test
        @DisplayName("Deve retornar 400 com body invalido — type faltando")
        void deveRetornar400BodyInvalido() throws Exception {
            // Missing required fields: type and occurredAt
            String invalidJson = "{\"location\":\"Santos\"}";

            mockMvc.perform(post("/api/v1/shipments/{shipmentId}/events", shipmentId)
                            .with(csrf())
                            .with(user(adminPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar 404 quando shipment inexistente")
        void deveRetornar404ShipmentInexistente() throws Exception {
            UUID unknownId = UUID.randomUUID();
            CreateEventRequest request = new CreateEventRequest(
                    EventType.LOADED,
                    "Santos, Brazil",
                    null,
                    Instant.now()
            );

            when(eventService.create(eq(unknownId), any(CreateEventRequest.class), eq(TestDataFactory.defaultTenantId()), eq(null)))
                    .thenThrow(new ResourceNotFoundException("Shipment", unknownId));

            mockMvc.perform(post("/api/v1/shipments/{shipmentId}/events", unknownId)
                            .with(csrf())
                            .with(user(adminPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("VIEWER nao pode criar evento")
        void viewerNaoPodeCriarEvento() throws Exception {
            CreateEventRequest request = new CreateEventRequest(
                    EventType.GATE_IN,
                    "Santos, Brazil",
                    "Container received at gate",
                    Instant.now()
            );

            mockMvc.perform(post("/api/v1/shipments/{shipmentId}/events", shipmentId)
                            .with(csrf())
                            .with(user(viewerPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("CLIENT nao pode criar evento")
        void clientNaoPodeCriarEvento() throws Exception {
            CreateEventRequest request = new CreateEventRequest(
                    EventType.GATE_IN,
                    "Santos, Brazil",
                    "Container received at gate",
                    Instant.now()
            );

            mockMvc.perform(post("/api/v1/shipments/{shipmentId}/events", shipmentId)
                            .with(csrf())
                            .with(user(clientPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== DELETE /api/v1/shipments/{shipmentId}/events/{eventId} ====================

    @Nested
    @DisplayName("DELETE /api/v1/shipments/{shipmentId}/events/{eventId}")
    class DeleteEvent {

        @Test
        @DisplayName("Deve retornar 204 ao deletar evento existente")
        void deveRetornar204() throws Exception {
            UUID eventId = UUID.randomUUID();
            doNothing().when(eventService).delete(shipmentId, eventId, TestDataFactory.defaultTenantId(), null);

            mockMvc.perform(delete("/api/v1/shipments/{shipmentId}/events/{eventId}",
                                    shipmentId, eventId)
                            .with(csrf())
                            .with(user(adminPrincipal)))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Deve retornar 404 ao deletar evento inexistente")
        void deveRetornar404EventoInexistente() throws Exception {
            UUID unknownEventId = UUID.randomUUID();
            doThrow(new ResourceNotFoundException("Event", unknownEventId))
                    .when(eventService).delete(shipmentId, unknownEventId, TestDataFactory.defaultTenantId(), null);

            mockMvc.perform(delete("/api/v1/shipments/{shipmentId}/events/{eventId}",
                                    shipmentId, unknownEventId)
                            .with(csrf())
                            .with(user(adminPrincipal)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("VIEWER nao pode deletar evento")
        void viewerNaoPodeDeletarEvento() throws Exception {
            UUID eventId = UUID.randomUUID();

            mockMvc.perform(delete("/api/v1/shipments/{shipmentId}/events/{eventId}",
                                    shipmentId, eventId)
                            .with(csrf())
                            .with(user(viewerPrincipal)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("CLIENT nao pode deletar evento")
        void clientNaoPodeDeletarEvento() throws Exception {
            UUID eventId = UUID.randomUUID();

            mockMvc.perform(delete("/api/v1/shipments/{shipmentId}/events/{eventId}",
                                    shipmentId, eventId)
                            .with(csrf())
                            .with(user(clientPrincipal)))
                    .andExpect(status().isForbidden());
        }
    }
}
