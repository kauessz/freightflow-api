package com.freightflow.modules.alert;

import com.freightflow.fixtures.TestDataFactory;
import com.freightflow.modules.alert.dto.AlertResponse;
import com.freightflow.modules.alert.dto.CreateAlertRequest;
import com.freightflow.modules.alert.enums.AlertType;
import com.freightflow.modules.alert.enums.Severity;
import com.freightflow.modules.auth.Tenant;
import com.freightflow.modules.shipment.Shipment;
import com.freightflow.modules.shipment.repository.ShipmentRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertService")
class AlertServiceTest {

    @Mock private AlertRepository alertRepository;
    @Mock private ShipmentRepository shipmentRepository;
    @Mock private AlertEventPublisher alertEventPublisher;

    @InjectMocks
    private AlertService alertService;

    private Tenant tenant;
    private Shipment shipment;

    @BeforeEach
    void setUp() {
        tenant   = TestDataFactory.tenant();
        shipment = TestDataFactory.shipment();
    }

    // ── create() ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("should_createAlert_when_noOpenAlertOfSameTypeExists")
        void should_createAlert_when_noOpenAlertOfSameTypeExists() {
            // Arrange
            UUID tenantId   = tenant.getId();
            UUID shipmentId = shipment.getId();
            CreateAlertRequest request = new CreateAlertRequest(
                    shipmentId, AlertType.DELAY, Severity.HIGH, "Vessel delayed by 24 hours");

            Alert saved = TestDataFactory.alert(shipment);
            TestDataFactory.setEntityId(saved, UUID.randomUUID());

            when(shipmentRepository.findByIdAndTenantId(shipmentId, tenantId))
                    .thenReturn(Optional.of(shipment));
            when(alertRepository.existsByShipmentIdAndTypeAndResolvedFalse(shipmentId, AlertType.DELAY))
                    .thenReturn(false);
            when(alertRepository.save(any(Alert.class))).thenReturn(saved);

            // Act
            AlertResponse result = alertService.create(request, tenantId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.type()).isEqualTo(AlertType.DELAY);
            assertThat(result.resolved()).isFalse();
        }

        @Test
        @DisplayName("should_throwBusinessException_when_openAlertOfSameTypeAlreadyExists")
        void should_throwBusinessException_when_openAlertOfSameTypeAlreadyExists() {
            // Arrange
            UUID tenantId   = tenant.getId();
            UUID shipmentId = shipment.getId();
            CreateAlertRequest request = new CreateAlertRequest(
                    shipmentId, AlertType.DELAY, Severity.MEDIUM, "Another delay");

            when(shipmentRepository.findByIdAndTenantId(shipmentId, tenantId))
                    .thenReturn(Optional.of(shipment));
            when(alertRepository.existsByShipmentIdAndTypeAndResolvedFalse(shipmentId, AlertType.DELAY))
                    .thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> alertService.create(request, tenantId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("open alert of type DELAY");
        }

        @Test
        @DisplayName("should_throwResourceNotFoundException_when_shipmentNotFoundOrCrossTenant")
        void should_throwResourceNotFoundException_when_shipmentNotFoundOrCrossTenant() {
            // Arrange — findByIdAndTenantId returns empty (shipment doesn't exist OR belongs to another tenant)
            UUID otherTenantId = UUID.randomUUID();
            UUID shipmentId    = shipment.getId();
            CreateAlertRequest request = new CreateAlertRequest(
                    shipmentId, AlertType.CUSTOMS_HOLD, Severity.CRITICAL, "Customs hold");

            when(shipmentRepository.findByIdAndTenantId(shipmentId, otherTenantId))
                    .thenReturn(Optional.empty());

            // Act & Assert — 404 to avoid revealing cross-tenant resource existence
            assertThatThrownBy(() -> alertService.create(request, otherTenantId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Shipment");
        }

        @Test
        @DisplayName("should_publishCriticalAlertEvent_when_severityIsCritical")
        void should_publishCriticalAlertEvent_when_severityIsCritical() {
            // Arrange
            UUID tenantId   = tenant.getId();
            UUID shipmentId = shipment.getId();
            CreateAlertRequest request = new CreateAlertRequest(
                    shipmentId, AlertType.CUSTOMS_HOLD, Severity.CRITICAL, "Customs hold at Santos");

            Alert saved = TestDataFactory.alert(shipment);
            TestDataFactory.setEntityId(saved, UUID.randomUUID());
            // Override severity to CRITICAL via reflection (TestDataFactory creates MEDIUM by default)
            setSeverity(saved, Severity.CRITICAL);

            when(shipmentRepository.findByIdAndTenantId(shipmentId, tenantId))
                    .thenReturn(Optional.of(shipment));
            when(alertRepository.existsByShipmentIdAndTypeAndResolvedFalse(shipmentId, AlertType.CUSTOMS_HOLD))
                    .thenReturn(false);
            when(alertRepository.save(any(Alert.class))).thenReturn(saved);

            // Act
            alertService.create(request, tenantId);

            // Assert — publisher must be called exactly once with the saved alert
            verify(alertEventPublisher, times(1)).publishCriticalAlert(saved);
        }

        @Test
        @DisplayName("should_notPublishEvent_when_severityIsLow")
        void should_notPublishEvent_when_severityIsLow() {
            // Arrange
            UUID tenantId   = tenant.getId();
            UUID shipmentId = shipment.getId();
            CreateAlertRequest request = new CreateAlertRequest(
                    shipmentId, AlertType.DELAY, Severity.LOW, "Minor delay, no action required");

            Alert saved = TestDataFactory.alert(shipment);
            TestDataFactory.setEntityId(saved, UUID.randomUUID());
            setSeverity(saved, Severity.LOW);

            when(shipmentRepository.findByIdAndTenantId(shipmentId, tenantId))
                    .thenReturn(Optional.of(shipment));
            when(alertRepository.existsByShipmentIdAndTypeAndResolvedFalse(shipmentId, AlertType.DELAY))
                    .thenReturn(false);
            when(alertRepository.save(any(Alert.class))).thenReturn(saved);

            // Act
            alertService.create(request, tenantId);

            // Assert — publisher must NOT be called for LOW severity
            verify(alertEventPublisher, never()).publishCriticalAlert(any());
        }
    }

    // ── resolve() ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("resolve()")
    class ResolveTests {

        @Test
        @DisplayName("should_resolveAlert_when_alertBelongsToTenant")
        void should_resolveAlert_when_alertBelongsToTenant() {
            // Arrange
            UUID tenantId = tenant.getId();
            Alert alert   = TestDataFactory.alert(shipment);
            UUID alertId  = UUID.randomUUID();
            TestDataFactory.setEntityId(alert, alertId);

            when(alertRepository.findById(alertId)).thenReturn(Optional.of(alert));
            when(alertRepository.save(any(Alert.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            AlertResponse result = alertService.resolve(alertId, tenantId);

            // Assert
            assertThat(result.resolved()).isTrue();
            assertThat(result.resolvedAt()).isNotNull();
        }

        @Test
        @DisplayName("should_throwBusinessException_when_alertIsAlreadyResolved")
        void should_throwBusinessException_when_alertIsAlreadyResolved() {
            // Arrange
            UUID tenantId = tenant.getId();
            Alert alert   = TestDataFactory.alert(shipment);
            UUID alertId  = UUID.randomUUID();
            TestDataFactory.setEntityId(alert, alertId);
            alert.resolve(); // pre-resolve

            when(alertRepository.findById(alertId)).thenReturn(Optional.of(alert));

            // Act & Assert
            assertThatThrownBy(() -> alertService.resolve(alertId, tenantId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("already resolved");
        }
    }

    // ── findOpenByTenant() ────────────────────────────────────────────────

    @Nested
    @DisplayName("findOpenByTenant()")
    class FindOpenByTenantTests {

        @Test
        @DisplayName("should_returnOnlyOpenAlerts_for_callerTenant")
        void should_returnOnlyOpenAlerts_for_callerTenant() {
            // Arrange
            UUID tenantId = tenant.getId();
            Alert alert1  = TestDataFactory.alert(shipment);
            Alert alert2  = TestDataFactory.alert(shipment);
            TestDataFactory.setEntityId(alert1, UUID.randomUUID());
            TestDataFactory.setEntityId(alert2, UUID.randomUUID());

            when(alertRepository.findOpenByTenantId(tenantId)).thenReturn(List.of(alert1, alert2));

            // Act
            List<AlertResponse> result = alertService.findOpenByTenant(tenantId);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(a -> !a.resolved());
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    /**
     * Sets the severity field via reflection.
     * Necessary because Alert has no setter for severity (immutable after construction
     * through the public constructor).
     */
    private void setSeverity(Alert alert, Severity severity) {
        try {
            var field = Alert.class.getDeclaredField("severity");
            field.setAccessible(true);
            field.set(alert, severity);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set severity on Alert", e);
        }
    }
}
