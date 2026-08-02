package com.freightflow.modules.platform.subscription.dto;

import com.freightflow.modules.platform.subscription.TenantSubscriptionStatus;

import java.time.Instant;
import java.util.UUID;

public record TenantSubscriptionResponse(
        UUID id,
        UUID tenantId,
        SubscriptionPlanSnapshotResponse plan,
        TenantSubscriptionStatus status,
        Instant startedAt,
        Instant endedAt,
        Instant createdAt,
        Instant updatedAt,
        String reason
) {
}
