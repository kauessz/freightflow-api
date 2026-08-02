package com.freightflow.modules.platform.entitlement.dto;

import com.freightflow.modules.platform.entitlement.TenantEntitlementAccessStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TenantEntitlementResolutionResponse(
        UUID tenantId,
        TenantEntitlementAccessStatus accessStatus,
        TenantEntitlementSubscriptionResponse subscription,
        List<TenantFeatureEntitlementResponse> features,
        List<String> warnings,
        Instant resolvedAt
) {
}
