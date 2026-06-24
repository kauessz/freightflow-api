package com.freightflow.modules.vessel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightflow.config.TestSecurityConfig;
import com.freightflow.fixtures.TestDataFactory;
import com.freightflow.modules.ais.dto.AisPositionResponse;
import com.freightflow.modules.ais.dto.PositionSource;
import com.freightflow.modules.shipment.dto.FleetMapShipmentResponse;
import com.freightflow.modules.vessel.dto.PositionTrackPoint;
import com.freightflow.modules.vessel.dto.VesselWithVoyageResponse;
import com.freightflow.modules.voyage.dto.FleetMapIneligibilityReason;
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
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link VesselController}.
 *
 * Focuses on the position track-history endpoint added in the Enhanced Tracking phase:
 *   - GET /api/v1/vessels/{imo}/track?limit=50
 */
@WebMvcTest(controllers = VesselController.class)
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = true)
@DisplayName("VesselController")
class VesselControllerTest {

    @Autowired private MockMvc      mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean  private VesselService          vesselService;
    @MockBean  private PositionHistoryService  positionHistoryService;

    private final UserPrincipal principal = TestDataFactory.principal();
    private final UserPrincipal clientPrincipal = new UserPrincipal(
            UUID.randomUUID(),
            "client@mercosul.com",
            "Client",
            TestDataFactory.defaultTenantId(),
            "CLIENT",
            UUID.randomUUID()
    );

    // ── helpers ───────────────────────────────────────────────────────────────

    private PositionTrackPoint samplePoint() {
        return new PositionTrackPoint(
                -22.5, -43.2,
                LocalDateTime.now().minusMinutes(5),
                14.5,
                TestDataFactory.defaultVoyageId().toString()
        );
    }

    // ==================== GET /api/v1/vessels/{imo}/track ====================

    @Nested
    @DisplayName("GET /api/v1/vessels/active-with-shipments")
    class ActiveWithShipmentsEndpoint {

        @Test
        @DisplayName("Deve retornar 200 com contrato consolidado do Fleet Map")
        void deveRetornar200ComContratoConsolidado() throws Exception {
            VesselWithVoyageResponse response = new VesselWithVoyageResponse(
                    TestDataFactory.defaultVesselId(),
                    "9839012",
                    "MSC Oscar",
                    "MSC Oscar",
                    "9839012",
                    "MSC",
                    -23.95,
                    -46.33,
                    Instant.parse("2026-06-24T12:00:00Z"),
                    PositionSource.LIVE,
                    false,
                    AisPositionResponse.live("9839012", -23.95, -46.33, 14.2, 90.0, "under_way", Instant.parse("2026-06-24T12:00:00Z")),
                    TestDataFactory.defaultVoyageId(),
                    "MSC-2026-001",
                    "IN_TRANSIT",
                    "Santos",
                    "BRSSZ",
                    -23.9536,
                    -46.3336,
                    "Rotterdam",
                    "NLRTM",
                    "Rotterdam",
                    "NLRTM",
                    51.9225,
                    4.4792,
                    Instant.parse("2026-06-20T12:00:00Z"),
                    Instant.parse("2026-07-10T12:00:00Z"),
                    1,
                    "HIGH",
                    List.of(new FleetMapShipmentResponse(
                            UUID.fromString("ffff0000-0000-0000-0000-000000000001"),
                            "A123456789",
                            "MSCU1234567",
                            "IN_TRANSIT",
                            "HIGH",
                            "MSC Oscar",
                            "MSC-2026-001",
                            "MSC",
                            "Santos",
                            "BRSSZ",
                            "Rotterdam",
                            "NLRTM",
                            Instant.parse("2026-07-10T12:00:00Z")
                    )),
                    true,
                    List.of()
            );

            when(vesselService.getActiveWithShipments(principal.getTenantId(), null))
                    .thenReturn(List.of(response));

            mockMvc.perform(get("/api/v1/vessels/active-with-shipments")
                            .with(user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].vesselId").value(TestDataFactory.defaultVesselId().toString()))
                    .andExpect(jsonPath("$[0].vesselName").value("MSC Oscar"))
                    .andExpect(jsonPath("$[0].vesselImo").value("9839012"))
                    .andExpect(jsonPath("$[0].carrier").value("MSC"))
                    .andExpect(jsonPath("$[0].positionSource").value("LIVE"))
                    .andExpect(jsonPath("$[0].vesselPosition.latitude").value(-23.95))
                    .andExpect(jsonPath("$[0].voyageId").value(TestDataFactory.defaultVoyageId().toString()))
                    .andExpect(jsonPath("$[0].etd").value("2026-06-20T12:00:00Z"))
                    .andExpect(jsonPath("$[0].eta").value("2026-07-10T12:00:00Z"))
                    .andExpect(jsonPath("$[0].aggregatedRiskLevel").value("HIGH"))
                    .andExpect(jsonPath("$[0].relatedShipments[0].booking").value("A123456789"))
                    .andExpect(jsonPath("$[0].relatedShipments[0].vesselName").value("MSC Oscar"))
                    .andExpect(jsonPath("$[0].relatedShipments[0].voyageNumber").value("MSC-2026-001"));
        }

        @Test
        @DisplayName("Deve aplicar customerId para CLIENT")
        void deveAplicarCustomerIdParaClient() throws Exception {
            when(vesselService.getActiveWithShipments(clientPrincipal.getTenantId(), clientPrincipal.getCustomerId()))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/v1/vessels/active-with-shipments")
                            .with(user(clientPrincipal)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Deve retornar 401 sem autenticacao")
        void deveRetornar401SemAutenticacao() throws Exception {
            mockMvc.perform(get("/api/v1/vessels/active-with-shipments"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/vessels/{imo}/track")
    class TrackEndpoint {

        @Test
        @DisplayName("Deve retornar 200 com lista de pontos de posicao")
        void deveRetornar200ComListaDePontos() throws Exception {
            String imo = "9839012";
            when(positionHistoryService.getPositionHistory(imo, 50))
                    .thenReturn(List.of(samplePoint(), samplePoint()));

            mockMvc.perform(get("/api/v1/vessels/imo/{imo}/track", imo)
                            .with(user(principal))
                            .param("limit", "50"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].lat").value(-22.5))
                    .andExpect(jsonPath("$[0].lon").value(-43.2))
                    .andExpect(jsonPath("$[0].speed").value(14.5));
        }

        @Test
        @DisplayName("Deve retornar 200 com lista vazia quando nao ha eventos de posicao")
        void deveRetornar200ListaVazia() throws Exception {
            String imo = "9839012";
            when(positionHistoryService.getPositionHistory(imo, 50))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/v1/vessels/imo/{imo}/track", imo)
                            .with(user(principal))
                            .param("limit", "50"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("Deve retornar 404 quando vessel nao encontrado pelo IMO")
        void deveRetornar404VesselNaoEncontrado() throws Exception {
            String unknownImo = "0000000";
            when(positionHistoryService.getPositionHistory(unknownImo, 50))
                    .thenThrow(new ResourceNotFoundException("Vessel", unknownImo));

            mockMvc.perform(get("/api/v1/vessels/imo/{imo}/track", unknownImo)
                            .with(user(principal))
                            .param("limit", "50"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Resource Not Found"));
        }

        @Test
        @DisplayName("Deve retornar 401 sem autenticacao")
        void deveRetornar401SemAuth() throws Exception {
            mockMvc.perform(get("/api/v1/vessels/imo/{imo}/track", "9839012"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
