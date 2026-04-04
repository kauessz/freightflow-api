package com.freightflow.modules.shipment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightflow.config.TestSecurityConfig;
import com.freightflow.fixtures.TestDataFactory;
import com.freightflow.modules.shipment.dto.CreateShipmentRequest;
import com.freightflow.modules.shipment.dto.ShipmentResponse;
import com.freightflow.modules.shipment.dto.UpdateShipmentRequest;
import com.freightflow.modules.shipment.enums.ContainerType;
import com.freightflow.modules.shipment.enums.ShipmentStatus;
import com.freightflow.modules.shipment.service.ShipmentService;
import com.freightflow.shared.exception.BusinessException;
import com.freightflow.shared.exception.GlobalExceptionHandler;
import com.freightflow.shared.exception.ResourceNotFoundException;
import com.freightflow.shared.pagination.PageResponse;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ShipmentController.class)
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = true)
@DisplayName("ShipmentController")
class ShipmentControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private ShipmentService shipmentService;

    private final UserPrincipal principal = TestDataFactory.principal();

    private ShipmentResponse sampleResponse() {
        return new ShipmentResponse(
                TestDataFactory.defaultShipmentId(),
                "A123456789",
                // documentos
                "HBL123456", "MBL789012", "REF-001",
                // container
                "MSCU1234567", ContainerType.TEU40, 40, "GP",
                BigDecimal.valueOf(24000), BigDecimal.valueOf(22000), BigDecimal.valueOf(38.5),
                400, "cartons",
                // status
                ShipmentStatus.BOOKED,
                "PENDING", "NOT_STARTED", "LOW", 0,
                // portos
                "Santos", "BRSSZ", "Rotterdam", "NLRTM", null, null,
                // partes
                "Shipper SA", "Consignee NV", "Notify Party NV", "Marina Rocha",
                // voyage
                "MSC Oscar", "MSC-2026-001", "MSC", "South America Loop",
                Instant.now().plusSeconds(86400),
                // comercial
                "CIF", "Prepaid", "General cargo",
                // misc
                "https://www.vesselfinder.com/vessels/details/9282261", "Direct service.",
                Instant.now(), Instant.now()
        );
    }

    // ==================== GET /api/v1/shipments ====================

    @Nested
    @DisplayName("GET /api/v1/shipments")
    class ListEndpoint {

        @Test
        @DisplayName("Deve retornar 200 com lista paginada de shipments")
        void deveRetornar200ComLista() throws Exception {
            var response = new PageResponse<>(
                    List.of(sampleResponse()),
                    new PageResponse.Meta(1, 0, 20, 1)
            );
            when(shipmentService.list(any(UUID.class), any())).thenReturn(response);

            mockMvc.perform(get("/api/v1/shipments")
                            .with(user(principal))
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].booking").value("A123456789"))
                    .andExpect(jsonPath("$.meta.total").value(1));
        }

        @Test
        @DisplayName("Deve retornar 401 sem autenticacao")
        void deveRetornar401SemAuth() throws Exception {
            mockMvc.perform(get("/api/v1/shipments"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== GET /api/v1/shipments/{id} ====================

    @Nested
    @DisplayName("GET /api/v1/shipments/{id}")
    class GetByIdEndpoint {

        @Test
        @DisplayName("Deve retornar 200 com shipment encontrado")
        void deveRetornar200() throws Exception {
            when(shipmentService.getById(any(UUID.class))).thenReturn(sampleResponse());

            mockMvc.perform(get("/api/v1/shipments/{id}", TestDataFactory.defaultShipmentId())
                            .with(user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.booking").value("A123456789"))
                    .andExpect(jsonPath("$.vesselName").value("MSC Oscar"));
        }

        @Test
        @DisplayName("Deve retornar 404 quando shipment nao encontrado")
        void deveRetornar404() throws Exception {
            UUID id = UUID.randomUUID();
            when(shipmentService.getById(id)).thenThrow(new ResourceNotFoundException("Shipment", id));

            mockMvc.perform(get("/api/v1/shipments/{id}", id)
                            .with(user(principal)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Resource Not Found"));
        }
    }

    // ==================== POST /api/v1/shipments ====================

    @Nested
    @DisplayName("POST /api/v1/shipments")
    class CreateEndpoint {

        @Test
        @DisplayName("Deve retornar 201 ao criar shipment valido")
        void deveRetornar201() throws Exception {
            CreateShipmentRequest request = new CreateShipmentRequest(
                    "B987654321", "CMAU7654321", ContainerType.TEU40HC,
                    TestDataFactory.defaultVoyageId(),
                    TestDataFactory.defaultPortOriginId(),
                    TestDataFactory.defaultPortDestId(),
                    "Consignee", "Shipper"
            );

            ShipmentResponse response = new ShipmentResponse(
                    UUID.randomUUID(),
                    "B987654321",
                    null, null, null,
                    "CMAU7654321", ContainerType.TEU40HC, 40, "HC",
                    null, null, null, null, null,
                    ShipmentStatus.BOOKED,
                    "PENDING", "NOT_STARTED", "LOW", 0,
                    "Santos", "BRSSZ", "Rotterdam", "NLRTM", null, null,
                    "Shipper", "Consignee", null, null,
                    "MSC Oscar", "MSC-2026-001", "MSC", null,
                    Instant.now().plusSeconds(86400),
                    null, null, null,
                    null, null,
                    Instant.now(), Instant.now()
            );

            when(shipmentService.create(any(CreateShipmentRequest.class), any(UUID.class)))
                    .thenReturn(response);

            mockMvc.perform(post("/api/v1/shipments")
                            .with(csrf())
                            .with(user(principal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.booking").value("B987654321"))
                    .andExpect(jsonPath("$.status").value("BOOKED"));
        }

        @Test
        @DisplayName("Deve retornar 400 com request invalido (booking faltando)")
        void deveRetornar400() throws Exception {
            String invalidJson = "{\"containerNumber\":\"MSCU1234567\",\"voyageId\":\"" + UUID.randomUUID() + "\"}";

            mockMvc.perform(post("/api/v1/shipments")
                            .with(csrf())
                            .with(user(principal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar 409 quando booking duplicado")
        void deveRetornar409() throws Exception {
            CreateShipmentRequest request = new CreateShipmentRequest(
                    "A123456789", "MSCU1234567", ContainerType.TEU40,
                    TestDataFactory.defaultVoyageId(),
                    TestDataFactory.defaultPortOriginId(),
                    TestDataFactory.defaultPortDestId(),
                    null, null
            );

            when(shipmentService.create(any(), any()))
                    .thenThrow(new BusinessException("Booking A123456789 already exists"));

            mockMvc.perform(post("/api/v1/shipments")
                            .with(csrf())
                            .with(user(principal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title").value("Business Rule Violation"));
        }
    }

    // ==================== DELETE /api/v1/shipments/{id} ====================

    @Nested
    @DisplayName("DELETE /api/v1/shipments/{id}")
    class DeleteEndpoint {

        @Test
        @DisplayName("Deve retornar 204 ao deletar shipment existente")
        void deveRetornar204() throws Exception {
            mockMvc.perform(delete("/api/v1/shipments/{id}", TestDataFactory.defaultShipmentId())
                            .with(csrf())
                            .with(user(principal)))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Deve retornar 404 ao deletar shipment inexistente")
        void deveRetornar404() throws Exception {
            UUID id = UUID.randomUUID();
            doThrow(new ResourceNotFoundException("Shipment", id))
                    .when(shipmentService).delete(id);

            mockMvc.perform(delete("/api/v1/shipments/{id}", id)
                            .with(csrf())
                            .with(user(principal)))
                    .andExpect(status().isNotFound());
        }
    }
}
