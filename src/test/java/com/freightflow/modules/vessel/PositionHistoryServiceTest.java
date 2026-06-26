package com.freightflow.modules.vessel;

import com.freightflow.fixtures.TestDataFactory;
import com.freightflow.modules.event.Event;
import com.freightflow.modules.event.enums.EventType;
import com.freightflow.modules.shipment.Shipment;
import com.freightflow.modules.shipment.repository.ShipmentRepository;
import com.freightflow.shared.exception.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PositionHistoryService")
class PositionHistoryServiceTest {

    @Mock
    private ShipmentRepository shipmentRepository;
    @Mock
    private EntityManager entityManager;
    @Mock
    private TypedQuery<Object[]> query;

    @InjectMocks
    private PositionHistoryService positionHistoryService;

    private final UUID tenantId = TestDataFactory.defaultTenantId();
    private final UUID customerId = UUID.randomUUID();
    private final UUID voyageId = TestDataFactory.defaultVoyageId();

    @Nested
    @DisplayName("getPositionHistory()")
    class GetPositionHistoryTests {

        @Test
        @DisplayName("should_returnTrackPoints_when_imoIsAccessibleForTenant")
        void should_returnTrackPoints_when_imoIsAccessibleForTenant() {
            String imo = "9839012";
            Event event = positionEvent("12.345678,-45.678912", "Position update — 14.5 kn, heading 270° (LIVE)");

            when(shipmentRepository.existsByVesselImoAndTenantId(imo, tenantId)).thenReturn(true);
            mockQuery(java.util.Collections.singletonList(new Object[]{event, voyageId}));

            var result = positionHistoryService.getPositionHistory(imo, tenantId, null, 50);

            assertThat(result).singleElement()
                    .satisfies(point -> {
                        assertThat(point.lat()).isEqualTo(12.345678);
                        assertThat(point.lon()).isEqualTo(-45.678912);
                        assertThat(point.speed()).isEqualTo(14.5);
                        assertThat(point.voyageId()).isEqualTo(voyageId.toString());
                        assertThat(point.occurredAt()).isEqualTo(LocalDateTime.ofInstant(event.getOccurredAt(), ZoneOffset.UTC));
                    });
        }

        @Test
        @DisplayName("should_filterMalformedCoordinates_withoutThrowing500")
        void should_filterMalformedCoordinates_withoutThrowing500() {
            String imo = "9839012";
            Event malformed = positionEvent("563150,41,362250", "Position update — 12.1 kn, heading 90° (LIVE)");
            Event valid = positionEvent("-22.500000,-43.200000", "Position update — 13.0 kn, heading 180° (LIVE)");

            when(shipmentRepository.existsByVesselImoAndTenantId(imo, tenantId)).thenReturn(true);
            mockQuery(List.of(
                    new Object[]{malformed, voyageId},
                    new Object[]{valid, voyageId}
            ));

            var result = positionHistoryService.getPositionHistory(imo, tenantId, null, 50);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).lat()).isEqualTo(-22.5);
            assertThat(result.get(0).lon()).isEqualTo(-43.2);
        }

        @Test
        @DisplayName("should_returnEmptyList_when_historyIsEmptyButVesselIsAccessible")
        void should_returnEmptyList_when_historyIsEmptyButVesselIsAccessible() {
            String imo = "9839012";

            when(shipmentRepository.existsByVesselImoAndTenantId(imo, tenantId)).thenReturn(true);
            mockQuery(List.of());

            var result = positionHistoryService.getPositionHistory(imo, tenantId, null, 50);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should_throwNotFound_when_tenantHasNoAccessToImo")
        void should_throwNotFound_when_tenantHasNoAccessToImo() {
            String imo = "9839012";

            when(shipmentRepository.existsByVesselImoAndTenantId(imo, tenantId)).thenReturn(false);

            assertThatThrownBy(() -> positionHistoryService.getPositionHistory(imo, tenantId, null, 50))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Vessel");
        }

        @Test
        @DisplayName("should_throwNotFound_when_clientHasNoAccessToOtherCustomer")
        void should_throwNotFound_when_clientHasNoAccessToOtherCustomer() {
            String imo = "9839012";

            when(shipmentRepository.existsByVesselImoAndTenantIdAndCustomerId(imo, tenantId, customerId))
                    .thenReturn(false);

            assertThatThrownBy(() -> positionHistoryService.getPositionHistory(imo, tenantId, customerId, 50))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Vessel");
        }

        @Test
        @DisplayName("should_applyCustomerScopeInExistenceCheckAndQuery")
        void should_applyCustomerScopeInExistenceCheckAndQuery() {
            String imo = "9839012";
            Event valid = positionEvent("-22.500000,-43.200000", "Position update — 13.0 kn, heading 180° (LIVE)");

            when(shipmentRepository.existsByVesselImoAndTenantIdAndCustomerId(imo, tenantId, customerId))
                    .thenReturn(true);
            mockQuery(java.util.Collections.singletonList(new Object[]{valid, voyageId}));

            positionHistoryService.getPositionHistory(imo, tenantId, customerId, 50);

            verify(shipmentRepository).existsByVesselImoAndTenantIdAndCustomerId(imo, tenantId, customerId);

            ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
            verify(entityManager).createQuery(queryCaptor.capture(), eq(Object[].class));
            assertThat(queryCaptor.getValue()).contains("s.customer.id = :customerId");
            verify(query).setParameter("customerId", customerId);
        }

        @Test
        @DisplayName("should_fallbackToDefaultLimit_when_limitIsZeroOrNegative")
        void should_fallbackToDefaultLimit_when_limitIsZeroOrNegative() {
            String imo = "9839012";

            when(shipmentRepository.existsByVesselImoAndTenantId(imo, tenantId)).thenReturn(true);
            mockQuery(List.of());

            positionHistoryService.getPositionHistory(imo, tenantId, null, 0);

            verify(query).setMaxResults(PositionHistoryService.DEFAULT_LIMIT);
        }
    }

    private void mockQuery(List<Object[]> rows) {
        when(entityManager.createQuery(anyString(), eq(Object[].class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        when(query.getResultList()).thenReturn(rows);
    }

    private Event positionEvent(String location, String description) {
        Shipment shipment = TestDataFactory.shipment(UUID.randomUUID(), "BOOK-" + UUID.randomUUID());
        return new Event(
                shipment,
                EventType.POSITION_UPDATE,
                location,
                description,
                Instant.parse("2026-06-24T12:00:00Z")
        );
    }
}
