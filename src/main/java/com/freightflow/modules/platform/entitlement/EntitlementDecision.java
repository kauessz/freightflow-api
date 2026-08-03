package com.freightflow.modules.platform.entitlement;

import java.util.UUID;

public record EntitlementDecision(
        UUID tenantId,
        String featureKey,
        EntitlementEnforcementMode enforcementMode,
        boolean entitled,
        boolean allowedByRollout,
        boolean allowed,
        TenantEntitlementAccessStatus accessStatus,
        EntitlementDenialReason denialReason
) {
}
