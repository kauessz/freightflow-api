package com.freightflow.modules.platform.entitlement;

import com.freightflow.shared.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class EntitlementEnforcementService {

    private static final Logger log = LoggerFactory.getLogger(EntitlementEnforcementService.class);

    private final TenantEntitlementResolverService tenantEntitlementResolverService;
    private final EntitlementEnforcementProperties properties;

    public EntitlementEnforcementService(TenantEntitlementResolverService tenantEntitlementResolverService,
                                         EntitlementEnforcementProperties properties) {
        this.tenantEntitlementResolverService = tenantEntitlementResolverService;
        this.properties = properties;
    }

    public EntitlementDecision check(UUID tenantId, String rawFeatureKey) {
        if (tenantId == null) {
            throw new BadRequestException("Parameter 'tenantId' must not be null.");
        }

        String featureKey = normalizeFeatureKey(rawFeatureKey);
        TenantEntitlementResolution resolution = tenantEntitlementResolverService.resolveTenantEntitlements(tenantId);
        ResolvedFeatureEntitlement feature = resolution.features().stream()
                .filter(item -> item.featureKey().equals(featureKey))
                .findFirst()
                .orElse(null);

        boolean entitled = resolution.accessStatus() == TenantEntitlementAccessStatus.ACTIVE
                && feature != null
                && feature.effectiveEnabled();
        EntitlementDenialReason denialReason = entitled
                ? EntitlementDenialReason.NONE
                : resolveDenialReason(resolution, feature);
        EntitlementEnforcementMode mode = properties.getEnforcementMode();
        boolean allowedByRollout = !entitled && mode != EntitlementEnforcementMode.ENFORCE;
        boolean allowed = entitled || allowedByRollout;

        EntitlementDecision decision = new EntitlementDecision(
                tenantId,
                featureKey,
                mode,
                entitled,
                allowedByRollout,
                allowed,
                resolution.accessStatus(),
                denialReason
        );

        if (mode == EntitlementEnforcementMode.AUDIT && !entitled) {
            log.warn(
                    "Entitlement audit deny candidate tenantId={} featureKey={} accessStatus={} denialReason={} enforcementMode={}",
                    tenantId,
                    featureKey,
                    resolution.accessStatus(),
                    denialReason,
                    mode
            );
        }

        return decision;
    }

    public void requireEnabled(UUID tenantId, String rawFeatureKey) {
        EntitlementDecision decision = check(tenantId, rawFeatureKey);
        if (!decision.allowed()) {
            throw new FeatureNotAvailableException(decision.featureKey());
        }
    }

    private String normalizeFeatureKey(String rawFeatureKey) {
        if (rawFeatureKey == null || rawFeatureKey.trim().isEmpty()) {
            throw new BadRequestException("Parameter 'featureKey' must not be blank.");
        }
        return rawFeatureKey.trim().toUpperCase(Locale.ROOT);
    }

    private EntitlementDenialReason resolveDenialReason(TenantEntitlementResolution resolution,
                                                        ResolvedFeatureEntitlement feature) {
        return switch (resolution.accessStatus()) {
            case NO_SUBSCRIPTION -> EntitlementDenialReason.NO_SUBSCRIPTION;
            case SUSPENDED -> EntitlementDenialReason.SUBSCRIPTION_SUSPENDED;
            case INCONSISTENT_SUBSCRIPTION -> EntitlementDenialReason.INCONSISTENT_SUBSCRIPTION;
            case ACTIVE -> resolveFeatureDenialReason(feature);
        };
    }

    private EntitlementDenialReason resolveFeatureDenialReason(ResolvedFeatureEntitlement feature) {
        if (feature == null) {
            return EntitlementDenialReason.FEATURE_NOT_FOUND;
        }
        if (!feature.grantedByPlan()) {
            return EntitlementDenialReason.FEATURE_NOT_GRANTED;
        }
        if (!feature.effectiveEnabled()) {
            return EntitlementDenialReason.FEATURE_NOT_EFFECTIVE;
        }
        return EntitlementDenialReason.NONE;
    }
}
