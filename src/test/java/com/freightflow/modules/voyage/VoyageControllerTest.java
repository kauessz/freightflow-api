package com.freightflow.modules.voyage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightflow.config.TestSecurityConfig;
import com.freightflow.fixtures.TestDataFactory;
import com.freightflow.modules.ais.VesselPositionResolver;
import com.freightflow.modules.ais.dto.AisPositionResponse;
import com.freightflow.modules.ais.dto.PositionSource;
import com.freightflow.modules.voyage.dto.RevisedEtaResponse;
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
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link VoyageController}.
 *
 * Focuses on the new tracking endpoints added in the Enhanced Tracking phase:
 *   - GET /api/v1/voyages/{id}/eta
 *
 * Uses @WebMvcTest + Mockito mocks for all service-layer dependencies.
 */
@WebMvcTest(controllers = VoyageController.class)
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = true)
@DisplayName("VoyageController — ETA endpoint")
class VoyageControllerTest {

    @Autowired private MockMvc      mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean  private VoyageService          voyageService;
    @MockBean  private VoyageRepository        voyageRepository;
    @MockBean  private VesselPositionResolver  vesselPositionResolver;
    @MockBean  private EtaCalculatorService    etaCalculatorService;

    private final UserPrincipal principal  = TestDataFactory.principal();
    private final UUID          voyageId   = TestDataFactory.defaultVoyageId();

    // ── helpers ───────────────────────────────────────────────────────────────

    /** A live AIS position off the coast of Brazil. */
    private AisPositionResponse livePosition() {
        return AisPositionResponse.live(
                "9839012", -22.5, -43.2, 14.5, 270.0, "underway", Instant.now());
    }

    private RevisedEtaResponse sampleEtaResponse(UUID voyageId) {
        return new RevisedEtaResponse(
                voyageId.toString(),
                "MSC-2026-001",
                LocalDateTime.now().plusDays(5),
                LocalDateTime.now().plusDays(6),
                5300.0,
                14.5,
                24L,
                1,
                PositionSource.LIVE.name(),
                -22.5,
                -43.2
        );
    }

    // ==================== GET /api/v1/voyages/{id}/eta ====================

    @Nested
    @DisplayName("GET /api/v1/voyages/{id}/eta")
    class EtaEndpoint {

        @Test
        @DisplayName("Deve retornar 200 com RevisedEtaResponse quando AIS disponivel")
        void deveRetornar200EtaDinamica() throws Exception {
            Voyage voyage = TestDataFactory.voyage(voyageId, "MSC-2026-001");

            when(voyageRepository.findByIdWithDetails(voyageId))
                    .thenReturn(Optional.of(voyage));
            when(vesselPositionResolver.resolveForVoyage(any(Voyage.class)))
                    .thenReturn(livePosition());
            when(etaCalculatorService.calculate(any(Voyage.class), any(AisPositionResponse.class)))
                    .thenReturn(sampleEtaResponse(voyageId));

            mockMvc.perform(get("/api/v1/voyages/{id}/eta", voyageId)
                            .with(user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.voyageId").value(voyageId.toString()))
                    .andExpect(jsonPath("$.voyageNumber").value("MSC-2026-001"))
                    .andExpect(jsonPath("$.distanceNm").value(5300.0))
                    .andExpect(jsonPath("$.speedKnots").value(14.5))
                    .andExpect(jsonPath("$.delayDays").value(1))
                    .andExpect(jsonPath("$.positionSource").value("LIVE"));
        }

        @Test
        @DisplayName("Deve retornar 409 quando vessel nao tem IMO cadastrado")
        void deveRetornar409VesselSemImo() throws Exception {
            // Create a voyage whose vessel has a blank IMO via reflection
            Voyage voyage = TestDataFactory.voyage(voyageId, "MSC-2026-001");
            TestDataFactory.setEntityId(voyage.getVessel(), voyage.getVessel().getId());
            // Force IMO to blank via reflection
            try {
                var imoField = voyage.getVessel().getClass().getDeclaredField("imo");
                imoField.setAccessible(true);
                imoField.set(voyage.getVessel(), "");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            when(voyageRepository.findByIdWithDetails(voyageId))
                    .thenReturn(Optional.of(voyage));

            // VoyageController throws BusinessException → 409 via GlobalExceptionHandler
            mockMvc.perform(get("/api/v1/voyages/{id}/eta", voyageId)
                            .with(user(principal)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title").value("Business Rule Violation"));
        }

        @Test
        @DisplayName("Deve retornar 422 quando AIS retorna UNAVAILABLE")
        void deveRetornar422AisIndisponivel() throws Exception {
            Voyage voyage = TestDataFactory.voyage(voyageId, "MSC-2026-001");

            when(voyageRepository.findByIdWithDetails(voyageId))
                    .thenReturn(Optional.of(voyage));
            when(vesselPositionResolver.resolveForVoyage(any(Voyage.class)))
                    .thenReturn(AisPositionResponse.unavailable("9839012"));

            mockMvc.perform(get("/api/v1/voyages/{id}/eta", voyageId)
                            .with(user(principal)))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.statusCode").value(422))
                    .andExpect(jsonPath("$.error").value("Unprocessable Entity"))
                    .andExpect(jsonPath("$.message").value("AIS position unavailable for this vessel"));
        }

        @Test
        @DisplayName("Deve retornar 404 quando voyage nao existe")
        void deveRetornar404VoyageNaoExiste() throws Exception {
            UUID unknownId = UUID.randomUUID();
            when(voyageRepository.findByIdWithDetails(unknownId))
                    .thenReturn(Optional.empty());

            mockMvc.perform(get("/api/v1/voyages/{id}/eta", unknownId)
                            .with(user(principal)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Deve retornar 401 sem autenticacao")
        void deveRetornar401SemAuth() throws Exception {
            mockMvc.perform(get("/api/v1/voyages/{id}/eta", voyageId))
                    .andExpect(status().isUnauthorized());
        }
    }
}
