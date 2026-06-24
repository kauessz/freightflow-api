package com.freightflow.modules.vessel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightflow.config.TestSecurityConfig;
import com.freightflow.fixtures.TestDataFactory;
import com.freightflow.modules.vessel.dto.PositionTrackPoint;
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

import java.time.LocalDateTime;
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
@DisplayName("VesselController — track endpoint")
class VesselControllerTest {

    @Autowired private MockMvc      mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean  private VesselService          vesselService;
    @MockBean  private PositionHistoryService  positionHistoryService;

    private final UserPrincipal principal = TestDataFactory.principal();

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
