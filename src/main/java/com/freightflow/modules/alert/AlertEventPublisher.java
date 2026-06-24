package com.freightflow.modules.alert;

import com.freightflow.config.RabbitMQConfig;
import com.freightflow.modules.alert.dto.AlertEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final RabbitTemplate rabbitTemplate;

    public AlertEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Builds an {@link AlertEvent} from the persisted alert and publishes it to RabbitMQ.
     *
     * @param alert the newly saved alert (must have a non-null ID and shipment)
     */
    public void publishCriticalAlert(Alert alert) {
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
