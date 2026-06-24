package com.freightflow.modules.alert;

import com.freightflow.modules.alert.dto.AlertResponse;
import com.freightflow.modules.alert.dto.CreateAlertRequest;
import com.freightflow.modules.alert.enums.Severity;
import com.freightflow.modules.shipment.Shipment;
import com.freightflow.modules.shipment.repository.ShipmentRepository;
import com.freightflow.shared.exception.BusinessException;
import com.freightflow.shared.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);

    private final AlertRepository alertRepository;
    private final ShipmentRepository shipmentRepository;
    private final AlertEventPublisher alertEventPublisher;

    public AlertService(AlertRepository alertRepository,
                        ShipmentRepository shipmentRepository,
                        AlertEventPublisher alertEventPublisher) {
        this.alertRepository = alertRepository;
        this.shipmentRepository = shipmentRepository;
        this.alertEventPublisher = alertEventPublisher;
    }

    // ==================== Queries ====================

    /**
     * Lista todos os alerts em aberto (resolved=false) do tenant.
     */
    public List<AlertResponse> findOpenByTenant(UUID tenantId) {
        log.debug("Listing open alerts for tenant={}", tenantId);
        return alertRepository.findOpenByTenantId(tenantId)
                .stream()
                .map(AlertResponse::from)
                .toList();
    }

    /**
     * Lista todos os alerts de um embarque específico.
     * Valida existência do shipment antes de buscar.
     */
    public List<AlertResponse> findByShipment(UUID shipmentId, UUID tenantId, UUID customerId) {
        log.debug("Listing alerts for shipment={}", shipmentId);
        Shipment shipment = getScopedShipment(shipmentId, tenantId, customerId);

        List<Alert> alerts = customerId != null
                ? alertRepository.findByShipmentIdAndShipmentTenantIdAndShipmentCustomerId(
                        shipment.getId(), tenantId, customerId)
                : alertRepository.findByShipmentIdAndShipmentTenantId(shipment.getId(), tenantId);

        return alerts
                .stream()
                .map(AlertResponse::from)
                .toList();
    }

    // ==================== Commands ====================

    /**
     * Cria um alert para um embarque.
     * Impede duplicatas: não pode existir alert aberto do mesmo tipo para o mesmo shipment.
     * Publica evento no RabbitMQ se severity == HIGH ou CRITICAL.
     */
    @Transactional
    public AlertResponse create(CreateAlertRequest request, UUID tenantId) {
        log.info("Creating alert type={} for shipment={}", request.type(), request.shipmentId());

        Shipment shipment = shipmentRepository.findByIdAndTenantId(request.shipmentId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", request.shipmentId()));

        if (alertRepository.existsByShipmentIdAndTypeAndResolvedFalse(request.shipmentId(), request.type())) {
            throw new BusinessException(
                    "An open alert of type " + request.type() + " already exists for shipment " + request.shipmentId());
        }

        Alert alert = new Alert(shipment, request.type(), request.severity(), request.message());
        Alert saved = alertRepository.save(alert);

        if (saved.getSeverity() == Severity.HIGH || saved.getSeverity() == Severity.CRITICAL) {
            alertEventPublisher.publishCriticalAlert(saved);
        }

        log.info("Alert created: id={}, type={}, shipment={}", saved.getId(), saved.getType(), shipment.getBooking());
        return AlertResponse.from(saved);
    }

    /**
     * Resolve um alert existente.
     * Valida que o alert pertence ao tenant do caller via shipment.
     */
    @Transactional
    public AlertResponse resolve(UUID alertId, UUID tenantId, UUID customerId) {
        log.info("Resolving alert id={} for tenant={}", alertId, tenantId);

        Alert alert = getScopedAlert(alertId, tenantId, customerId);

        if (alert.isResolved()) {
            throw new BusinessException("Alert " + alertId + " is already resolved");
        }

        alert.resolve();
        Alert saved = alertRepository.save(alert);

        log.info("Alert resolved: id={}", saved.getId());
        return AlertResponse.from(saved);
    }

    private Shipment getScopedShipment(UUID shipmentId, UUID tenantId, UUID customerId) {
        if (customerId != null) {
            return shipmentRepository.findByIdAndTenantIdAndCustomerId(shipmentId, tenantId, customerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Shipment", shipmentId));
        }
        return shipmentRepository.findByIdAndTenantId(shipmentId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", shipmentId));
    }

    private Alert getScopedAlert(UUID alertId, UUID tenantId, UUID customerId) {
        if (customerId != null) {
            return alertRepository.findByIdAndShipmentTenantIdAndShipmentCustomerId(alertId, tenantId, customerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Alert", alertId));
        }
        return alertRepository.findByIdAndShipmentTenantId(alertId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert", alertId));
    }
}
