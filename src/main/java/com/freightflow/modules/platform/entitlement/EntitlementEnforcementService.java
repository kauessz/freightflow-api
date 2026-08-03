package com.freightflow.modules.platform.entitlement;

import com.freightflow.shared.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

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

        return checkAll(tenantId, List.of(rawFeatureKey)).decisions().getFirst();
    }

    public EntitlementBatchDecision checkAll(UUID tenantId, Collection<String> rawFeatureKeys) {
        if (tenantId == null) {
            throw new BadRequestException("Parameter 'tenantId' must not be null.");
        }

        List<String> featureKeys = normalizeFeatureKeys(rawFeatureKeys);
        TenantEntitlementResolution resolution = tenantEntitlementResolverService.resolveTenantEntitlements(tenantId);
        EntitlementEnforcementMode mode = properties.getEnforcementMode();

        List<EntitlementDecision> decisions = featureKeys.stream()
                .map(featureKey -> buildDecision(tenantId, featureKey, resolution, mode))
                .toList();

        boolean entitled = decisions.stream().allMatch(EntitlementDecision::entitled);
        boolean allowedByRollout = !entitled && mode != EntitlementEnforcementMode.ENFORCE;
        boolean allowed = entitled || allowedByRollout;
        String firstDeniedFeatureKey = decisions.stream()
                .filter(decision -> !decision.entitled())
                .map(EntitlementDecision::featureKey)
                .findFirst()
                .orElse(null);

        if (mode == EntitlementEnforcementMode.AUDIT) {
            decisions.stream()
                    .filter(decision -> !decision.entitled())
                    .forEach(decision -> log.warn(
                            "Entitlement audit deny candidate tenantId={} featureKey={} accessStatus={} denialReason={} enforcementMode={}",
                            tenantId,
                            decision.featureKey(),
                            decision.accessStatus(),
                            decision.denialReason(),
                            mode
                    ));
        }

        return new EntitlementBatchDecision(
                tenantId,
                mode,
                featureKeys,
                decisions,
                entitled,
                allowedByRollout,
                allowed,
                firstDeniedFeatureKey
        );
    }

    public void requireEnabled(UUID tenantId, String rawFeatureKey) {
        requireAllEnabled(tenantId, List.of(rawFeatureKey));
    }

    public void requireAllEnabled(UUID tenantId, Collection<String> rawFeatureKeys) {
        EntitlementBatchDecision decision = checkAll(tenantId, rawFeatureKeys);
        if (!decision.allowed()) {
            throw new FeatureNotAvailableException(decision.firstDeniedFeatureKey());
        }
    }

    private String normalizeFeatureKey(String rawFeatureKey) {
        if (rawFeatureKey == null || rawFeatureKey.trim().isEmpty()) {
            throw new BadRequestException("Parameter 'featureKey' must not be blank.");
        }
        return rawFeatureKey.trim().toUpperCase(Locale.ROOT);
    }

    private List<String> normalizeFeatureKeys(Collection<String> rawFeatureKeys) {
        if (rawFeatureKeys == null || rawFeatureKeys.isEmpty()) {
            throw new BadRequestException("Parameter 'featureKeys' must not be empty.");
        }
        return rawFeatureKeys.stream()
                .map(this::normalizeFeatureKey)
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(LinkedHashSet::new),
                        keys -> keys.stream()
                                .sorted(Comparator.naturalOrder())
                                .toList()
                ));
    }

    private EntitlementDecision buildDecision(UUID tenantId,
                                              String featureKey,
                                              TenantEntitlementResolution resolution,
                                              EntitlementEnforcementMode mode) {
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
        boolean allowedByRollout = !entitled && mode != EntitlementEnforcementMode.ENFORCE;
        boolean allowed = entitled || allowedByRollout;

        return new EntitlementDecision(
                tenantId,
                featureKey,
                mode,
                entitled,
                allowedByRollout,
                allowed,
                resolution.accessStatus(),
                denialReason
        );
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
