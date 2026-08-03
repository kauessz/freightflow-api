package com.freightflow.modules.platform.entitlement;

import java.util.List;
import java.util.UUID;

public record EntitlementBatchDecision(
        UUID tenantId,
        EntitlementEnforcementMode enforcementMode,
        List<String> featureKeys,
        List<EntitlementDecision> decisions,
        boolean entitled,
        boolean allowedByRollout,
        boolean allowed,
        String firstDeniedFeatureKey
) {
}
