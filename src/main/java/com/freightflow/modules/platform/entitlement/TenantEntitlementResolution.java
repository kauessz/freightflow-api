package com.freightflow.modules.platform.entitlement;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TenantEntitlementResolution(
        UUID tenantId,
        TenantEntitlementAccessStatus accessStatus,
        ResolvedTenantSubscription subscription,
        List<ResolvedFeatureEntitlement> features,
        List<String> warnings,
        Instant resolvedAt
) {
}
