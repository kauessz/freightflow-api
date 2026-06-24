package com.freightflow.modules.webhook;

import com.freightflow.modules.alert.Alert;
import com.freightflow.modules.shipment.Shipment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Bridge between domain events (alert created, shipment status changed) and
 * outbound webhook calls.
 *
 * <p>Constructs a lightweight, self-contained payload map so subscribers receive
 * all the context they need without knowing the internal domain model.
 *
 * <p>All calls delegate to {@link WebhookService#notifySubscribers}, which is
 * {@code @Async} — this class therefore never blocks the calling thread.
 */
@Component
public class WebhookEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(WebhookEventPublisher.class);

    /** Event type names used as the X-FreightFlow-Event header value. */
    public static final String EVENT_ALERT_CRITICAL     = "ALERT_CRITICAL";
    public static final String EVENT_SHIPMENT_DELIVERED = "SHIPMENT_DELIVERED";
    public static final String EVENT_SHIPMENT_CANCELLED = "SHIPMENT_CANCELLED";

    private final WebhookService webhookService;

    public WebhookEventPublisher(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    /**
     * Publishes an {@code ALERT_CRITICAL} webhook event.
     *
     * <p>Called by {@link com.freightflow.modules.alert.AlertEventConsumer}
     * after a HIGH/CRITICAL alert is received from RabbitMQ.
     *
     * @param alert the persisted alert entity
     */
    public void publishAlertCritical(Alert alert) {
        UUID tenantId = alert.getShipment().getTenant().getId();

        Map<String, Object> payload = Map.of(
                "event",      EVENT_ALERT_CRITICAL,
                "alertId",    alert.getId().toString(),
                "shipmentId", alert.getShipment().getId().toString(),
                "booking",    alert.getShipment().getBooking(),
                "type",       alert.getType().name(),
                "severity",   alert.getSeverity().name(),
                "message",    alert.getMessage(),
                "occurredAt", Instant.now().toString()
        );

        log.debug("Publishing webhook event={} for tenant={} alert={}",
                EVENT_ALERT_CRITICAL, tenantId, alert.getId());

        webhookService.notifySubscribers(tenantId, EVENT_ALERT_CRITICAL, payload);
    }

    /**
     * Publishes a {@code SHIPMENT_DELIVERED} or {@code SHIPMENT_CANCELLED} webhook event.
     *
     * <p>Called by {@link com.freightflow.modules.shipment.service.ShipmentService}
     * when a shipment transitions to DELIVERED or CANCELLED status.
     *
     * @param shipment  the updated shipment entity (new status already set)
     * @param eventType either {@link #EVENT_SHIPMENT_DELIVERED} or {@link #EVENT_SHIPMENT_CANCELLED}
     */
    public void publishShipmentStatusChange(Shipment shipment, String eventType) {
        UUID tenantId = shipment.getTenant().getId();

        Map<String, Object> payload = Map.of(
                "event",      eventType,
                "shipmentId", shipment.getId().toString(),
                "booking",    shipment.getBooking(),
                "status",     shipment.getStatus().name(),
                "occurredAt", Instant.now().toString()
        );

        log.debug("Publishing webhook event={} for tenant={} shipment={}",
                eventType, tenantId, shipment.getId());

        webhookService.notifySubscribers(tenantId, eventType, payload);
    }
}
