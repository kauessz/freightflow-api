package com.freightflow.modules.alert.dto;

import com.freightflow.modules.alert.enums.AlertType;
import com.freightflow.modules.alert.enums.Severity;

import java.time.Instant;
import java.util.UUID;

/**
 * Event payload published to RabbitMQ when an alert with severity HIGH or CRITICAL is created.
 *
 * Serialized as JSON by Jackson2JsonMessageConverter.
 * Consumers deserialize back to this record automatically via the configured MessageConverter.
 */
public record AlertEvent(
        UUID alertId,
        UUID shipmentId,
        AlertType type,
        Severity severity,
        String message,
        Instant createdAt
) {}
