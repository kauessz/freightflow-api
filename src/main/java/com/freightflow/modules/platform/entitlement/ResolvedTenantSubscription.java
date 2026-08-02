package com.freightflow.modules.platform.entitlement;

import com.freightflow.modules.platform.catalog.SubscriptionPlanStatus;
import com.freightflow.modules.platform.subscription.TenantSubscriptionStatus;

import java.time.Instant;
import java.util.UUID;

public record ResolvedTenantSubscription(
        UUID id,
        TenantSubscriptionStatus status,
        UUID planId,
        String planCode,
        SubscriptionPlanStatus planStatus,
        Instant startedAt,
        Instant endedAt
) {
}
