package com.freightflow.modules.webhook.dto;

import com.freightflow.modules.webhook.WebhookSubscription;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for webhook subscription resources.
 * The secret is intentionally omitted for security — never returned to callers.
 */
public record WebhookResponse(

        UUID id,
        String url,
        List<String> events,
        boolean active,
        int failureCount,
        Instant lastTriggeredAt,
        Instant createdAt,
        Instant updatedAt

) {
    public static WebhookResponse from(WebhookSubscription sub) {
        List<String> eventList = Arrays.stream(sub.getEvents().split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        return new WebhookResponse(
                sub.getId(),
                sub.getUrl(),
                eventList,
                sub.isActive(),
                sub.getFailureCount(),
                sub.getLastTriggeredAt(),
                sub.getCreatedAt(),
                sub.getUpdatedAt()
        );
    }
}
