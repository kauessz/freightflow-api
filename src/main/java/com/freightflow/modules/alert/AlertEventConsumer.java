package com.freightflow.modules.alert;

import com.freightflow.config.RabbitMQConfig;
import com.freightflow.modules.alert.dto.AlertEvent;
import com.freightflow.modules.notification.EmailService;
import com.freightflow.modules.webhook.WebhookEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Consumes critical alert events from RabbitMQ.
 *
 * Listens on queue "freightflow.alerts.critical".
 * Messages are deserialized automatically by the Jackson2JsonMessageConverter
 * configured in RabbitMQConfig — no manual ObjectMapper needed.
 *
 * Current behaviour:
 *   1. Structured log with full alert context.
 *   2. Fires outbound webhooks for all subscribers listening to ALERT_CRITICAL.
 *   3. Sends an e-mail notification to the customer contact address (if
 *      {@link EmailService} is configured and the shipment has a linked customer).
 *
 * Both webhook dispatch and e-mail delivery are best-effort: exceptions are
 * caught and logged so they never poison the RabbitMQ message and trigger redelivery.
 */
@Component
@ConditionalOnProperty(prefix = "freightflow.messaging", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AlertEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(AlertEventConsumer.class);

    private final AlertRepository        alertRepository;
    private final WebhookEventPublisher  webhookEventPublisher;

    /**
     * Null when {@code spring.mail.username} is not set (local dev / Railway
     * without SMTP configured). Callers must null-check before use.
     */
    @Autowired(required = false)
    private EmailService emailService;

    public AlertEventConsumer(AlertRepository alertRepository,
                              WebhookEventPublisher webhookEventPublisher) {
        this.alertRepository       = alertRepository;
        this.webhookEventPublisher = webhookEventPublisher;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void handleCriticalAlert(AlertEvent event) {
        log.warn("CRITICAL ALERT RECEIVED — shipment={}, type={}, severity={}: {}",
                event.shipmentId(), event.type(), event.severity(), event.message());

        // Dispatch outbound webhooks for all subscribers of this tenant that listen
        // to ALERT_CRITICAL. Load the full Alert entity so the publisher has access
        // to tenant information for routing.
        alertRepository.findById(event.alertId()).ifPresentOrElse(
                alert -> {
                    // 1. Webhook dispatch
                    try {
                        webhookEventPublisher.publishAlertCritical(alert);
                    } catch (Exception ex) {
                        // Webhook dispatch is best-effort — never let it fail the consumer
                        log.warn("Webhook dispatch failed for alertId={}: {}", event.alertId(), ex.getMessage());
                    }

                    // 2. E-mail notification to customer contact
                    if (emailService != null) {
                        try {
                            var shipment = alert.getShipment();
                            var customer = shipment != null ? shipment.getCustomer() : null;
                            String contactEmail = customer != null ? customer.getContactEmail() : null;

                            if (contactEmail != null && !contactEmail.isBlank()) {
                                emailService.sendAlertNotification(
                                        contactEmail,
                                        shipment.getBooking(),
                                        alert.getType().name(),
                                        alert.getMessage()
                                );
                            }
                        } catch (Exception ex) {
                            // E-mail is best-effort — never block or requeue the message
                            log.warn("Email notification failed for alertId={}: {}", event.alertId(), ex.getMessage());
                        }
                    }
                },
                () -> log.warn("Alert not found for webhook dispatch: alertId={}", event.alertId())
        );
    }
}
