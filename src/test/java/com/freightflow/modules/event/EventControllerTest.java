package com.freightflow.modules.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightflow.config.TestSecurityConfig;
import com.freightflow.fixtures.TestDataFactory;
import com.freightflow.modules.event.dto.CreateEventRequest;
import com.freightflow.modules.event.dto.EventResponse;
import com.freightflow.modules.event.enums.EventType;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EventController.class)
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = true)
@DisplayName("EventController")
class EventControllerTest {

    @Autowired private MockMvc      mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean  private EventService eventService;

    private final UserPrincipal  principal  = TestDataFactory.principal();
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
            when(eventService.listByShipment(shipmentId))
                    .thenReturn(List.of(sampleEvent(eventId)));

            mockMvc.perform(get("/api/v1/shipments/{shipmentId}/events", shipmentId)
                            .with(user(principal)))
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
            when(eventService.listByShipment(unknownId))
                    .thenThrow(new ResourceNotFoundException("Shipment", unknownId));

            mockMvc.perform(get("/api/v1/shipments/{shipmentId}/events", unknownId)
                            .with(user(principal)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Resource Not Found"));
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
            when(eventService.getById(eventId)).thenReturn(sampleEvent(eventId));

            mockMvc.perform(get("/api/v1/shipments/{shipmentId}/events/{eventId}",
                                    shipmentId, eventId)
                            .with(user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(eventId.toString()))
                    .andExpect(jsonPath("$.type").value("GATE_IN"))
                    .andExpect(jsonPath("$.booking").value("A123456789"));
        }

        @Test
        @DisplayName("Deve retornar 404 quando evento nao existe")
        void deveRetornar404EventoInexistente() throws Exception {
            UUID unknownEventId = UUID.randomUUID();
            when(eventService.getById(unknownEventId))
                    .thenThrow(new ResourceNotFoundException("Event", unknownEventId));

            mockMvc.perform(get("/api/v1/shipments/{shipmentId}/events/{eventId}",
                                    shipmentId, unknownEventId)
                            .with(user(principal)))
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

            when(eventService.create(eq(shipmentId), any(CreateEventRequest.class)))
                    .thenReturn(sampleEvent(eventId));

            mockMvc.perform(post("/api/v1/shipments/{shipmentId}/events", shipmentId)
                            .with(csrf())
                            .with(user(principal))
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
                            .with(user(principal))
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

            when(eventService.create(eq(unknownId), any(CreateEventRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Shipment", unknownId));

            mockMvc.perform(post("/api/v1/shipments/{shipmentId}/events", unknownId)
                            .with(csrf())
                            .with(user(principal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
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
            doNothing().when(eventService).delete(eventId);

            mockMvc.perform(delete("/api/v1/shipments/{shipmentId}/events/{eventId}",
                                    shipmentId, eventId)
                            .with(csrf())
                            .with(user(principal)))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Deve retornar 404 ao deletar evento inexistente")
        void deveRetornar404EventoInexistente() throws Exception {
            UUID unknownEventId = UUID.randomUUID();
            doThrow(new ResourceNotFoundException("Event", unknownEventId))
                    .when(eventService).delete(unknownEventId);

            mockMvc.perform(delete("/api/v1/shipments/{shipmentId}/events/{eventId}",
                                    shipmentId, unknownEventId)
                            .with(csrf())
                            .with(user(principal)))
                    .andExpect(status().isNotFound());
        }
    }
}
