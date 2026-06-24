package com.freightflow.modules.analytics;

import com.freightflow.modules.alert.enums.Severity;
import com.freightflow.modules.analytics.dto.DelayStatsResponse;
import com.freightflow.modules.analytics.dto.OperationsDashboardResponse;
import com.freightflow.modules.analytics.dto.PerformanceResponse;
import com.freightflow.modules.shipment.enums.ShipmentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AnalyticsService}.
 *
 * Note: {@code @Cacheable} is NOT active in Mockito unit tests — Spring AOP proxying
 * is bypassed when calling the service directly via {@code @InjectMocks}.
 * Each test call therefore goes straight to the (mocked) repository, which is what
 * we want for isolated logic verification.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyticsService")
class AnalyticsServiceTest {

    @Mock
    private AnalyticsRepository analyticsRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private UUID tenantId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.fromString("aaaa0000-0000-0000-0000-000000000001");
    }

    // ── getOperationsDashboard() ──────────────────────────────────────────

    @Nested
    @DisplayName("getOperationsDashboard()")
    class OperationsDashboardTests {

        @Test
        @DisplayName("should return correct scalar counts when repository returns valid data")
        void should_returnCorrectScalarCounts_when_repositoryReturnsData() {
            // Arrange
            when(analyticsRepository.countActiveShipments(tenantId)).thenReturn(42L);
            when(analyticsRepository.countInTransit(tenantId)).thenReturn(15L);
            when(analyticsRepository.countDelayed(tenantId)).thenReturn(7L);
            when(analyticsRepository.countAtRisk(tenantId)).thenReturn(3L);
            when(analyticsRepository.countAwaitingDocs(tenantId)).thenReturn(5L);
            when(analyticsRepository.countOpenAlerts(tenantId)).thenReturn(8L);
            when(analyticsRepository.countOpenAlertsBySeverity(tenantId, Severity.CRITICAL)).thenReturn(2L);
            when(analyticsRepository.countOpenAlertsBySeverity(tenantId, Severity.HIGH)).thenReturn(4L);
            when(analyticsRepository.countByStatus(tenantId)).thenReturn(List.of(
                    new Object[]{ShipmentStatus.IN_TRANSIT, 15L},
                    new Object[]{ShipmentStatus.BOOKED, 10L}
            ));
            when(analyticsRepository.countByCarrierTop5(tenantId)).thenReturn(List.of(
                    new Object[]{"MSC", 20L},
                    new Object[]{"CMA CGM", 12L}
            ));

            // Act
            OperationsDashboardResponse result = analyticsService.getOperationsDashboard(tenantId);

            // Assert scalar fields
            assertThat(result.totalShipments()).isEqualTo(42L);
            assertThat(result.inTransit()).isEqualTo(15L);
            assertThat(result.delayed()).isEqualTo(7L);
            assertThat(result.atRisk()).isEqualTo(3L);
            assertThat(result.awaitingDocs()).isEqualTo(5L);
            assertThat(result.openAlerts()).isEqualTo(8L);
            assertThat(result.criticalAlerts()).isEqualTo(2L);
            assertThat(result.highAlerts()).isEqualTo(4L);

            // Assert maps
            assertThat(result.byStatus())
                    .containsEntry("IN_TRANSIT", 15L)
                    .containsEntry("BOOKED", 10L);
            assertThat(result.byCarrier())
                    .containsEntry("MSC", 20L)
                    .containsEntry("CMA CGM", 12L);
        }

        @Test
        @DisplayName("should return empty maps when no data exists")
        void should_returnEmptyMaps_when_noDataExists() {
            // Arrange — all zeros
            when(analyticsRepository.countActiveShipments(tenantId)).thenReturn(0L);
            when(analyticsRepository.countInTransit(tenantId)).thenReturn(0L);
            when(analyticsRepository.countDelayed(tenantId)).thenReturn(0L);
            when(analyticsRepository.countAtRisk(tenantId)).thenReturn(0L);
            when(analyticsRepository.countAwaitingDocs(tenantId)).thenReturn(0L);
            when(analyticsRepository.countOpenAlerts(tenantId)).thenReturn(0L);
            when(analyticsRepository.countOpenAlertsBySeverity(tenantId, Severity.CRITICAL)).thenReturn(0L);
            when(analyticsRepository.countOpenAlertsBySeverity(tenantId, Severity.HIGH)).thenReturn(0L);
            when(analyticsRepository.countByStatus(tenantId)).thenReturn(List.of());
            when(analyticsRepository.countByCarrierTop5(tenantId)).thenReturn(List.of());

            // Act
            OperationsDashboardResponse result = analyticsService.getOperationsDashboard(tenantId);

            // Assert
            assertThat(result.totalShipments()).isZero();
            assertThat(result.byStatus()).isEmpty();
            assertThat(result.byCarrier()).isEmpty();
        }

        @Test
        @DisplayName("should delegate to repository with the caller's tenantId")
        void should_delegateToRepository_with_callerTenantId() {
            // Arrange
            stubAllDashboardCounts();

            // Act
            analyticsService.getOperationsDashboard(tenantId);

            // Assert — verify repository was called with the right tenantId
            verify(analyticsRepository).countActiveShipments(tenantId);
            verify(analyticsRepository).countOpenAlerts(tenantId);
            verify(analyticsRepository).countByCarrierTop5(tenantId);
        }
    }

    // ── getDelayStats() ───────────────────────────────────────────────────

    @Nested
    @DisplayName("getDelayStats()")
    class DelayStatsTests {

        @Test
        @DisplayName("should calculate overallDelayRate correctly — 3 delayed of 10 active = 30.0%")
        void should_calculateDelayRate_correctly() {
            // Arrange
            List<Object[]> routes = new ArrayList<>();
            routes.add(new Object[]{"BRSSZ", "ARBUE", 5L, 2L, 1.5});

            List<Object[]> vessels = new ArrayList<>();
            vessels.add(new Object[]{"MSC Oscar", "9839012", 5L, 1L, 0.5});

            when(analyticsRepository.countActiveShipments(tenantId)).thenReturn(10L);
            when(analyticsRepository.countDelayed(tenantId)).thenReturn(3L);
            when(analyticsRepository.delayStatsByRoute(tenantId)).thenReturn(routes);
            when(analyticsRepository.delayStatsByVessel(tenantId)).thenReturn(vessels);

            // Act
            DelayStatsResponse result = analyticsService.getDelayStats(tenantId);

            // Assert
            assertThat(result.overallDelayRate()).isEqualTo(30.0);

            // Route: 2 delayed of 5 = 40.0%
            assertThat(result.byRoute()).hasSize(1);
            DelayStatsResponse.RouteDelayStat route = result.byRoute().get(0);
            assertThat(route.originUnlocode()).isEqualTo("BRSSZ");
            assertThat(route.destUnlocode()).isEqualTo("ARBUE");
            assertThat(route.totalShipments()).isEqualTo(5L);
            assertThat(route.delayedShipments()).isEqualTo(2L);
            assertThat(route.avgDelayDays()).isEqualTo(1.5);
            assertThat(route.delayRate()).isEqualTo(40.0);

            // Vessel: 1 delayed of 5 = 20.0%
            assertThat(result.byVessel()).hasSize(1);
            DelayStatsResponse.VesselDelayStat vessel = result.byVessel().get(0);
            assertThat(vessel.vesselName()).isEqualTo("MSC Oscar");
            assertThat(vessel.imo()).isEqualTo("9839012");
            assertThat(vessel.delayRate()).isEqualTo(20.0);
        }

        @Test
        @DisplayName("should return 0.0 overallDelayRate when no active shipments exist")
        void should_returnZeroDelayRate_when_noActiveShipments() {
            // Arrange
            List<Object[]> emptyRoutes = new ArrayList<>();
            List<Object[]> emptyVessels = new ArrayList<>();

            when(analyticsRepository.countActiveShipments(tenantId)).thenReturn(0L);
            when(analyticsRepository.countDelayed(tenantId)).thenReturn(0L);
            when(analyticsRepository.delayStatsByRoute(tenantId)).thenReturn(emptyRoutes);
            when(analyticsRepository.delayStatsByVessel(tenantId)).thenReturn(emptyVessels);

            // Act
            DelayStatsResponse result = analyticsService.getDelayStats(tenantId);

            // Assert — no division by zero
            assertThat(result.overallDelayRate()).isEqualTo(0.0);
            assertThat(result.byRoute()).isEmpty();
            assertThat(result.byVessel()).isEmpty();
        }

        @Test
        @DisplayName("should return 0.0 routeDelayRate when route has no shipments (defensive)")
        void should_returnZeroRouteDelayRate_when_routeHasNoShipments() {
            // Arrange — unusual edge case: row with total=0
            List<Object[]> zeroRoutes = new ArrayList<>();
            zeroRoutes.add(new Object[]{"NLRTM", "CNSHA", 0L, 0L, 0.0});

            List<Object[]> emptyVessels2 = new ArrayList<>();

            when(analyticsRepository.countActiveShipments(tenantId)).thenReturn(5L);
            when(analyticsRepository.countDelayed(tenantId)).thenReturn(1L);
            when(analyticsRepository.delayStatsByRoute(tenantId)).thenReturn(zeroRoutes);
            when(analyticsRepository.delayStatsByVessel(tenantId)).thenReturn(emptyVessels2);

            // Act
            DelayStatsResponse result = analyticsService.getDelayStats(tenantId);

            // Assert — no division by zero
            assertThat(result.byRoute().get(0).delayRate()).isEqualTo(0.0);
        }
    }

    // ── getPerformance() ──────────────────────────────────────────────────

    @Nested
    @DisplayName("getPerformance()")
    class PerformanceTests {

        @Test
        @DisplayName("should calculate onTimeRate correctly — 8 on-time of 10 delivered = 80.0%")
        void should_calculateOnTimeRate_correctly() {
            // Arrange
            when(analyticsRepository.countDelivered(tenantId)).thenReturn(10L);
            when(analyticsRepository.countCancelled(tenantId)).thenReturn(2L);
            when(analyticsRepository.countDeliveredOnTime(tenantId)).thenReturn(8L);
            when(analyticsRepository.avgDelayDaysAll(tenantId)).thenReturn(1.5);
            when(analyticsRepository.onTimeRateByCarrier(tenantId)).thenReturn(List.of(
                    new Object[]{"MSC", 85.0},
                    new Object[]{"Hapag-Lloyd", 90.5}
            ));

            // Act
            PerformanceResponse result = analyticsService.getPerformance(tenantId);

            // Assert
            assertThat(result.onTimeRate()).isEqualTo(80.0);
            assertThat(result.totalDelivered()).isEqualTo(10L);
            assertThat(result.totalCancelled()).isEqualTo(2L);
            assertThat(result.avgDelayDays()).isEqualTo(1.5);
            assertThat(result.onTimeByCarrier())
                    .containsEntry("MSC", 85.0)
                    .containsEntry("Hapag-Lloyd", 90.5);
        }

        @Test
        @DisplayName("should return 100.0 onTimeRate when no shipments have been delivered yet")
        void should_return100_onTimeRate_when_noDeliveries() {
            // Arrange
            when(analyticsRepository.countDelivered(tenantId)).thenReturn(0L);
            when(analyticsRepository.countCancelled(tenantId)).thenReturn(0L);
            when(analyticsRepository.countDeliveredOnTime(tenantId)).thenReturn(0L);
            when(analyticsRepository.avgDelayDaysAll(tenantId)).thenReturn(0.0);
            when(analyticsRepository.onTimeRateByCarrier(tenantId)).thenReturn(List.of());

            // Act
            PerformanceResponse result = analyticsService.getPerformance(tenantId);

            // Assert — 100.0 as a meaningful default when the denominator is 0
            assertThat(result.onTimeRate()).isEqualTo(100.0);
            assertThat(result.onTimeByCarrier()).isEmpty();
        }

        @Test
        @DisplayName("should round onTimeRate to one decimal place — 1 on-time of 3 = 33.3%")
        void should_roundOnTimeRate_toOneDecimalPlace() {
            // Arrange
            when(analyticsRepository.countDelivered(tenantId)).thenReturn(3L);
            when(analyticsRepository.countCancelled(tenantId)).thenReturn(0L);
            when(analyticsRepository.countDeliveredOnTime(tenantId)).thenReturn(1L);
            when(analyticsRepository.avgDelayDaysAll(tenantId)).thenReturn(2.0);
            when(analyticsRepository.onTimeRateByCarrier(tenantId)).thenReturn(List.of());

            // Act
            PerformanceResponse result = analyticsService.getPerformance(tenantId);

            // Assert — 1/3 * 100 = 33.3333… → 33.3
            assertThat(result.onTimeRate()).isEqualTo(33.3);
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────

    /**
     * Stubs all repository calls needed by getOperationsDashboard() to minimal/zero values.
     * Use this when you only need the method to not throw, not to verify specific values.
     */
    private void stubAllDashboardCounts() {
        when(analyticsRepository.countActiveShipments(tenantId)).thenReturn(0L);
        when(analyticsRepository.countInTransit(tenantId)).thenReturn(0L);
        when(analyticsRepository.countDelayed(tenantId)).thenReturn(0L);
        when(analyticsRepository.countAtRisk(tenantId)).thenReturn(0L);
        when(analyticsRepository.countAwaitingDocs(tenantId)).thenReturn(0L);
        when(analyticsRepository.countOpenAlerts(tenantId)).thenReturn(0L);
        when(analyticsRepository.countOpenAlertsBySeverity(tenantId, Severity.CRITICAL)).thenReturn(0L);
        when(analyticsRepository.countOpenAlertsBySeverity(tenantId, Severity.HIGH)).thenReturn(0L);
        when(analyticsRepository.countByStatus(tenantId)).thenReturn(List.of());
        when(analyticsRepository.countByCarrierTop5(tenantId)).thenReturn(List.of());
    }
}
