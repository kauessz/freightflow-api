package com.freightflow.modules.voyage;

import com.freightflow.fixtures.TestDataFactory;
import com.freightflow.modules.ais.VesselPositionResolver;
import com.freightflow.modules.ais.dto.AisPositionResponse;
import com.freightflow.modules.ais.dto.PositionSource;
import com.freightflow.modules.ais.dto.VoyageTrackingResponse;
import com.freightflow.modules.port.Port;
import com.freightflow.modules.port.PortRepository;
import com.freightflow.modules.shipment.Shipment;
import com.freightflow.modules.shipment.dto.ShipmentSummaryResponse;
import com.freightflow.modules.shipment.repository.ShipmentRepository;
import com.freightflow.modules.vessel.Vessel;
import com.freightflow.modules.vessel.VesselRepository;
import com.freightflow.modules.voyage.dto.CreateVoyageRequest;
import com.freightflow.modules.voyage.dto.UpdateVoyageRequest;
import com.freightflow.modules.voyage.dto.VoyageResponse;
import com.freightflow.modules.voyage.enums.VoyageStatus;
import com.freightflow.shared.exception.BusinessException;
import com.freightflow.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("VoyageService")
class VoyageServiceTest {

    @Mock private VoyageRepository voyageRepository;
    @Mock private VesselRepository vesselRepository;
    @Mock private PortRepository portRepository;
    @Mock private ShipmentRepository shipmentRepository;
    @Mock private VesselPositionResolver vesselPositionResolver;

    @InjectMocks
    private VoyageService voyageService;

    private Vessel vessel;
    private Port origin;
    private Port destination;
    private Voyage voyage;

    @BeforeEach
    void setUp() {
        vessel      = TestDataFactory.vessel();
        origin      = TestDataFactory.santos();
        destination = TestDataFactory.rotterdam();
        voyage      = TestDataFactory.voyage();
    }

    // ── getById() ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getById()")
    class GetByIdTests {

        @Test
        @DisplayName("should_throwResourceNotFoundException_when_voyageNotFound")
        void should_throwResourceNotFoundException_when_voyageNotFound() {
            // Arrange
            UUID id = UUID.randomUUID();
            when(voyageRepository.findByIdWithDetails(id)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> voyageService.getById(id))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Voyage");
        }
    }

    @Nested
    @DisplayName("getTracking()")
    class GetTrackingTests {

        @Test
        @DisplayName("should_returnTrackingWithUnavailablePosition_when_aisFailsWithoutFallbackBase")
        void should_returnTrackingWithUnavailablePosition_when_aisFailsWithoutFallbackBase() {
            AisPositionResponse position = AisPositionResponse.unavailable(vessel.getImo());
            when(voyageRepository.findByIdWithDetails(voyage.getId())).thenReturn(Optional.of(voyage));
            when(vesselPositionResolver.resolveForVoyage(voyage, true)).thenReturn(position);

            VoyageTrackingResponse result = voyageService.getTracking(voyage.getId());

            assertThat(result.vesselPosition().positionSource()).isEqualTo(PositionSource.UNAVAILABLE);
        }
    }

    // ── create() ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("should_createVoyage_when_dataIsValid")
        void should_createVoyage_when_dataIsValid() {
            // Arrange
            Instant etd = Instant.now().plus(2, ChronoUnit.DAYS);
            Instant eta = Instant.now().plus(10, ChronoUnit.DAYS);
            CreateVoyageRequest request = new CreateVoyageRequest(
                    "MSC-2026-001", vessel.getId(), origin.getId(), destination.getId(), etd, eta);

            when(voyageRepository.existsByVoyageNumber("MSC-2026-001")).thenReturn(false);
            when(vesselRepository.findById(vessel.getId())).thenReturn(Optional.of(vessel));
            when(portRepository.findById(origin.getId())).thenReturn(Optional.of(origin));
            when(portRepository.findById(destination.getId())).thenReturn(Optional.of(destination));
            when(voyageRepository.save(any(Voyage.class))).thenReturn(voyage);

            // Act
            VoyageResponse result = voyageService.create(request);

            // Assert
            assertThat(result.voyageNumber()).isEqualTo(voyage.getVoyageNumber());
        }

        @Test
        @DisplayName("should_throwBusinessException_when_etdIsInThePast")
        void should_throwBusinessException_when_etdIsInThePast() {
            // Arrange
            Instant pastEtd = Instant.now().minus(1, ChronoUnit.HOURS);
            Instant eta     = Instant.now().plus(10, ChronoUnit.DAYS);
            CreateVoyageRequest request = new CreateVoyageRequest(
                    "MSC-2026-002", vessel.getId(), origin.getId(), destination.getId(), pastEtd, eta);
            when(voyageRepository.existsByVoyageNumber("MSC-2026-002")).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> voyageService.create(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("ETD cannot be in the past");
        }

        @Test
        @DisplayName("should_throwBusinessException_when_etaBeforeEtd")
        void should_throwBusinessException_when_etaBeforeEtd() {
            // Arrange
            Instant etd = Instant.now().plus(5, ChronoUnit.DAYS);
            Instant eta = Instant.now().plus(2, ChronoUnit.DAYS);   // ETA before ETD
            CreateVoyageRequest request = new CreateVoyageRequest(
                    "MSC-2026-003", vessel.getId(), origin.getId(), destination.getId(), etd, eta);
            when(voyageRepository.existsByVoyageNumber("MSC-2026-003")).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> voyageService.create(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("ETA must be after ETD");
        }

        @Test
        @DisplayName("should_throwBusinessException_when_duplicateVoyageNumber")
        void should_throwBusinessException_when_duplicateVoyageNumber() {
            // Arrange
            when(voyageRepository.existsByVoyageNumber("MSC-2026-001")).thenReturn(true);
            CreateVoyageRequest request = new CreateVoyageRequest(
                    "MSC-2026-001", vessel.getId(), origin.getId(), destination.getId(),
                    Instant.now().plus(2, ChronoUnit.DAYS),
                    Instant.now().plus(10, ChronoUnit.DAYS));

            // Act & Assert
            assertThatThrownBy(() -> voyageService.create(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("MSC-2026-001");
        }
    }

    // ── update() — status transitions ─────────────────────────────────────

    @Nested
    @DisplayName("update() — status transitions")
    class StatusTransitionTests {

        private void mockFindVoyageWithStatus(VoyageStatus status) {
            TestDataFactory.setEntityId(voyage, UUID.randomUUID());
            voyage.setStatus(status);
            when(voyageRepository.findByIdWithDetails(voyage.getId()))
                    .thenReturn(Optional.of(voyage));
            when(voyageRepository.save(any(Voyage.class))).thenReturn(voyage);
        }

        @Test
        @DisplayName("should_transitionStatus_SCHEDULED_to_DEPARTED")
        void should_transitionStatus_SCHEDULED_to_DEPARTED() {
            // Arrange — voyage starts SCHEDULED
            mockFindVoyageWithStatus(VoyageStatus.SCHEDULED);
            UpdateVoyageRequest request = new UpdateVoyageRequest(VoyageStatus.DEPARTED, null, null, null, null);

            // Act
            VoyageResponse result = voyageService.update(voyage.getId(), request);

            // Assert
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("should_transitionStatus_DEPARTED_to_IN_TRANSIT")
        void should_transitionStatus_DEPARTED_to_IN_TRANSIT() {
            mockFindVoyageWithStatus(VoyageStatus.DEPARTED);
            UpdateVoyageRequest request = new UpdateVoyageRequest(VoyageStatus.IN_TRANSIT, null, null, null, null);

            voyageService.update(voyage.getId(), request);
            // No exception = success
        }

        @Test
        @DisplayName("should_transitionStatus_when_sequenceIsValid_ARRIVED_to_COMPLETED")
        void should_transitionStatus_when_sequenceIsValid_ARRIVED_to_COMPLETED() {
            // Arrange
            mockFindVoyageWithStatus(VoyageStatus.ARRIVED);
            UpdateVoyageRequest request = new UpdateVoyageRequest(VoyageStatus.COMPLETED, null, null, null, null);

            // Act & Assert — no exception expected
            voyageService.update(voyage.getId(), request);
        }

        @Test
        @DisplayName("should_throwBusinessException_when_statusTransitionIsInvalid_ARRIVED_to_SCHEDULED")
        void should_throwBusinessException_when_statusTransitionIsInvalid() {
            // Arrange — ARRIVED → SCHEDULED is invalid
            mockFindVoyageWithStatus(VoyageStatus.ARRIVED);
            UpdateVoyageRequest request = new UpdateVoyageRequest(VoyageStatus.SCHEDULED, null, null, null, null);

            // Act & Assert
            assertThatThrownBy(() -> voyageService.update(voyage.getId(), request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Invalid status transition");
        }

        @Test
        @DisplayName("should_throwBusinessException_when_statusTransitionIsInvalid_COMPLETED_to_anything")
        void should_throwBusinessException_when_completedVoyageCannotTransition() {
            // Arrange — COMPLETED is terminal, no transitions allowed
            mockFindVoyageWithStatus(VoyageStatus.COMPLETED);
            UpdateVoyageRequest request = new UpdateVoyageRequest(VoyageStatus.ARRIVED, null, null, null, null);

            // Act & Assert
            assertThatThrownBy(() -> voyageService.update(voyage.getId(), request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Invalid status transition");
        }
    }

    @Nested
    @DisplayName("getShipmentsByVoyage()")
    class GetShipmentsByVoyageTests {

        @Test
        @DisplayName("should_filterByCustomer_when_customerIdIsProvided")
        void should_filterByCustomer_when_customerIdIsProvided() {
            UUID tenantId = TestDataFactory.defaultTenantId();
            UUID customerId = UUID.randomUUID();
            Shipment shipment = TestDataFactory.shipment();

            when(voyageRepository.findById(voyage.getId())).thenReturn(Optional.of(voyage));
            when(shipmentRepository.findByVoyageIdAndTenantIdAndCustomerId(voyage.getId(), tenantId, customerId))
                    .thenReturn(List.of(shipment));

            List<ShipmentSummaryResponse> result = voyageService.getShipmentsByVoyage(voyage.getId(), tenantId, customerId);

            assertThat(result).hasSize(1);
            verify(shipmentRepository).findByVoyageIdAndTenantIdAndCustomerId(voyage.getId(), tenantId, customerId);
        }
    }
}
