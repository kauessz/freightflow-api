package com.freightflow.modules.alert;

import com.freightflow.config.RabbitMQConfig;
import com.freightflow.modules.alert.dto.AlertEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes alert events to RabbitMQ.
 *
 * Single responsibility: convert an Alert entity into an AlertEvent payload and
 * send it to the "freightflow.alerts" exchange with routing key "alert.critical".
 *
 * Called by AlertService exclusively for alerts with severity HIGH or CRITICAL.
 */
@Component
public class AlertEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(AlertEventPublisher.class);

    private final ObjectProvider<RabbitTemplate> rabbitTemplateProvider;
    private final boolean messagingEnabled;

    public AlertEventPublisher(ObjectProvider<RabbitTemplate> rabbitTemplateProvider,
                               @Value("${freightflow.messaging.enabled:true}") boolean messagingEnabled) {
        this.rabbitTemplateProvider = rabbitTemplateProvider;
        this.messagingEnabled = messagingEnabled;
    }

    /**
     * Builds an {@link AlertEvent} from the persisted alert and publishes it to RabbitMQ.
     *
     * @param alert the newly saved alert (must have a non-null ID and shipment)
     */
    public void publishCriticalAlert(Alert alert) {
        if (!messagingEnabled) {
            log.debug("Messaging disabled — skipping RabbitMQ publish for alertId={}", alert.getId());
            return;
        }

        RabbitTemplate rabbitTemplate = rabbitTemplateProvider.getIfAvailable();
        if (rabbitTemplate == null) {
            throw new IllegalStateException(
                    "RabbitTemplate not available while freightflow.messaging.enabled=true");
        }

        AlertEvent event = new AlertEvent(
                alert.getId(),
                alert.getShipment().getId(),
                alert.getType(),
                alert.getSeverity(),
                alert.getMessage(),
                alert.getCreatedAt()
        );

        log.info("Publishing critical alert event: alertId={}, severity={}",
                event.alertId(), event.severity());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY,
                event
        );
    }
}
