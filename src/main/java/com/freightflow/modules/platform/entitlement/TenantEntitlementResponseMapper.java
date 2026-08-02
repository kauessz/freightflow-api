package com.freightflow.modules.platform.entitlement;

import com.freightflow.modules.platform.entitlement.dto.TenantEntitlementResolutionResponse;
import com.freightflow.modules.platform.entitlement.dto.TenantEntitlementSubscriptionResponse;
import com.freightflow.modules.platform.entitlement.dto.TenantFeatureEntitlementResponse;

public final class TenantEntitlementResponseMapper {

    private TenantEntitlementResponseMapper() {
    }

    public static TenantEntitlementResolutionResponse toResponse(TenantEntitlementResolution resolution) {
        return new TenantEntitlementResolutionResponse(
                resolution.tenantId(),
                resolution.accessStatus(),
                resolution.subscription() == null ? null : new TenantEntitlementSubscriptionResponse(
                        resolution.subscription().id(),
                        resolution.subscription().status(),
                        resolution.subscription().planId(),
                        resolution.subscription().planCode(),
                        resolution.subscription().planStatus(),
                        resolution.subscription().startedAt(),
                        resolution.subscription().endedAt()
                ),
                resolution.features().stream()
                        .map(feature -> new TenantFeatureEntitlementResponse(
                                feature.featureKey(),
                                feature.name(),
                                feature.valueType(),
                                feature.implementationStatus(),
                                feature.featureActive(),
                                feature.grantedByPlan(),
                                feature.effectiveEnabled(),
                                feature.limitValue(),
                                feature.unlimited(),
                                feature.dependencies(),
                                feature.unmetDependencies(),
                                feature.warnings()
                        ))
                        .toList(),
                resolution.warnings(),
                resolution.resolvedAt()
        );
    }
}
