package com.freightflow.modules.shipment;

import com.freightflow.config.TestSecurityConfig;
import com.freightflow.modules.event.enums.EventType;
import com.freightflow.modules.shipment.dto.PublicTrackingResponse;
import com.freightflow.modules.shipment.enums.ShipmentStatus;
import com.freightflow.modules.shipment.service.ShipmentService;
import com.freightflow.shared.exception.GlobalExceptionHandler;
import com.freightflow.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TrackingController.class)
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = true)
@DisplayName("TrackingController")
class TrackingControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private ShipmentService shipmentService;

    @Test
    @DisplayName("Deve retornar 200 para booking existente com payload publico sanitizado")
    void deveRetornar200ComPayloadSanitizado() throws Exception {
        PublicTrackingResponse response = new PublicTrackingResponse(
                "A123456789",
                "MSCU1234567",
                ShipmentStatus.IN_TRANSIT,
                "Shipment in transit",
                "MSC Oscar",
                "MSC-2026-001",
                "Santos",
                "BRSSZ",
                "Rotterdam",
                "NLRTM",
                Instant.parse("2026-06-24T12:00:00Z"),
                Instant.parse("2026-07-10T12:00:00Z"),
                Instant.parse("2026-06-25T12:00:00Z"),
                List.of(new PublicTrackingResponse.PublicTrackingMilestone(
                        EventType.LOADED,
                        "Santos, BR",
                        Instant.parse("2026-06-25T12:00:00Z")
                ))
        );

        when(shipmentService.track("A123456789")).thenReturn(response);

        mockMvc.perform(get("/api/v1/tracking/{booking}", "A123456789"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.booking").value("A123456789"))
                .andExpect(jsonPath("$.containerNumber").value("MSCU1234567"))
                .andExpect(jsonPath("$.status").value("IN_TRANSIT"))
                .andExpect(jsonPath("$.statusMessage").value("Shipment in transit"))
                .andExpect(jsonPath("$.milestones[0].type").value("LOADED"))
                .andExpect(jsonPath("$.houseBl").doesNotExist())
                .andExpect(jsonPath("$.masterBl").doesNotExist())
                .andExpect(jsonPath("$.documentStatus").doesNotExist())
                .andExpect(jsonPath("$.customsStatus").doesNotExist())
                .andExpect(jsonPath("$.riskLevel").doesNotExist())
                .andExpect(jsonPath("$.tenantId").doesNotExist())
                .andExpect(jsonPath("$.customerId").doesNotExist())
                .andExpect(jsonPath("$.milestones[0].description").doesNotExist())
                .andExpect(jsonPath("$.milestones[0].reportedAt").doesNotExist());
    }

    @Test
    @DisplayName("Deve retornar 404 para booking inexistente")
    void deveRetornar404ParaBookingInexistente() throws Exception {
        when(shipmentService.track("UNKNOWN"))
                .thenThrow(new ResourceNotFoundException("Shipment", "UNKNOWN"));

        mockMvc.perform(get("/api/v1/tracking/{booking}", "UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"));
    }
}
