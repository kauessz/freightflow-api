package com.freightflow.modules.platform.subscription.dto;

import com.freightflow.modules.platform.subscription.TenantSubscriptionEventType;
import com.freightflow.modules.platform.subscription.TenantSubscriptionStatus;

import java.time.Instant;
import java.util.UUID;

public record TenantSubscriptionEventResponse(
        UUID id,
        TenantSubscriptionEventType eventType,
        String previousPlanCode,
        String newPlanCode,
        TenantSubscriptionStatus previousStatus,
        TenantSubscriptionStatus newStatus,
        String reason,
        Instant createdAt
) {
}
