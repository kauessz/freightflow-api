package com.freightflow.modules.platform.entitlement.dto;

import com.freightflow.modules.platform.catalog.PlatformFeatureImplementationStatus;
import com.freightflow.modules.platform.catalog.PlatformFeatureValueType;

import java.util.List;

public record TenantFeatureEntitlementResponse(
        String featureKey,
        String name,
        PlatformFeatureValueType valueType,
        PlatformFeatureImplementationStatus implementationStatus,
        boolean featureActive,
        boolean grantedByPlan,
        boolean effectiveEnabled,
        Integer limitValue,
        boolean unlimited,
        List<String> dependencies,
        List<String> unmetDependencies,
        List<String> warnings
) {
}
