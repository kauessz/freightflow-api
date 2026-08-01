package com.freightflow.modules.platform.catalog.dto;

import com.freightflow.modules.platform.catalog.PlatformFeatureImplementationStatus;
import com.freightflow.modules.platform.catalog.PlatformFeatureValueType;

import java.util.List;

public record PlanEntitlementResponse(
        String featureKey,
        String featureName,
        String description,
        PlatformFeatureValueType valueType,
        String unit,
        PlatformFeatureImplementationStatus implementationStatus,
        List<String> dependencies,
        boolean enabled,
        Integer limitValue
) {
}
