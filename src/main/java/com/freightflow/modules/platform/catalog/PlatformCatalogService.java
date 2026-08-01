package com.freightflow.modules.platform.catalog;

import com.freightflow.modules.platform.catalog.dto.PlanEntitlementResponse;
import com.freightflow.modules.platform.catalog.dto.PlatformFeatureResponse;
import com.freightflow.modules.platform.catalog.dto.SubscriptionPlanDetailResponse;
import com.freightflow.modules.platform.catalog.dto.SubscriptionPlanSummaryResponse;
import com.freightflow.shared.exception.ResourceNotFoundException;
import com.freightflow.shared.pagination.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PlatformCatalogService {

    private final PlatformFeatureRepository platformFeatureRepository;
    private final PlatformFeatureDependencyRepository platformFeatureDependencyRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    public PlatformCatalogService(PlatformFeatureRepository platformFeatureRepository,
                                  PlatformFeatureDependencyRepository platformFeatureDependencyRepository,
                                  SubscriptionPlanRepository subscriptionPlanRepository) {
        this.platformFeatureRepository = platformFeatureRepository;
        this.platformFeatureDependencyRepository = platformFeatureDependencyRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
    }

    public PageResponse<PlatformFeatureResponse> listFeatures(Boolean active,
                                                              PlatformFeatureValueType valueType,
                                                              Pageable pageable) {
        Page<PlatformFeature> page;
        if (active != null && valueType != null) {
            page = platformFeatureRepository.findByActiveAndValueType(active, valueType, pageable);
        } else if (active != null) {
            page = platformFeatureRepository.findByActive(active, pageable);
        } else if (valueType != null) {
            page = platformFeatureRepository.findByValueType(valueType, pageable);
        } else {
            page = platformFeatureRepository.findAll(pageable);
        }

        Map<String, List<String>> dependencyMap = dependenciesByFeature(
                page.getContent().stream().map(PlatformFeature::getKey).toList()
        );

        return PageResponse.from(page.map(feature -> toFeatureResponse(feature, dependencyMap)));
    }

    public PlatformFeatureResponse getFeatureByKey(String key) {
        PlatformFeature feature = platformFeatureRepository.findById(normalizeCatalogKey(key))
                .orElseThrow(() -> new ResourceNotFoundException("Platform feature", key));

        Map<String, List<String>> dependencyMap = dependenciesByFeature(List.of(feature.getKey()));
        return toFeatureResponse(feature, dependencyMap);
    }

    public PageResponse<SubscriptionPlanSummaryResponse> listPlans(SubscriptionPlanStatus status, Pageable pageable) {
        Page<SubscriptionPlan> page = status == null
                ? subscriptionPlanRepository.findAll(pageable)
                : subscriptionPlanRepository.findByStatus(status, pageable);

        return PageResponse.from(page.map(this::toPlanSummaryResponse));
    }

    public SubscriptionPlanDetailResponse getPlanById(UUID id) {
        validateCatalogConsistency();

        SubscriptionPlan plan = subscriptionPlanRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription plan", id));

        return toPlanDetailResponse(plan);
    }

    public SubscriptionPlanDetailResponse getPlanByCode(String code) {
        validateCatalogConsistency();

        SubscriptionPlan plan = subscriptionPlanRepository.findDetailedByCode(normalizeCatalogKey(code))
                .orElseThrow(() -> new ResourceNotFoundException("Subscription plan", code));

        return toPlanDetailResponse(plan);
    }

    public void validateCatalogConsistency() {
        detectDependencyCycles(platformFeatureDependencyRepository.findAllWithRequiredFeature());
    }

    private SubscriptionPlanDetailResponse toPlanDetailResponse(SubscriptionPlan plan) {
        List<PlatformFeature> catalogFeatures = platformFeatureRepository.findAll().stream()
                .sorted(Comparator.comparing(PlatformFeature::getKey))
                .toList();

        Map<String, List<String>> dependencyMap = dependenciesByFeature(
                catalogFeatures.stream().map(PlatformFeature::getKey).toList()
        );

        Map<String, PlanEntitlement> entitlementsByFeature = plan.getEntitlements().stream()
                .collect(Collectors.toMap(entitlement -> entitlement.getFeature().getKey(), entitlement -> entitlement));

        List<PlanEntitlementResponse> entitlements = new ArrayList<>(catalogFeatures.size());
        for (PlatformFeature feature : catalogFeatures) {
            PlanEntitlement entitlement = entitlementsByFeature.get(feature.getKey());
            entitlements.add(toPlanEntitlementResponse(feature, entitlement, dependencyMap));
        }

        return new SubscriptionPlanDetailResponse(
                plan.getId(),
                plan.getCode(),
                plan.getName(),
                plan.getDescription(),
                plan.getStatus(),
                plan.getDisplayOrder(),
                plan.isCustom(),
                entitlements
        );
    }

    private PlatformFeatureResponse toFeatureResponse(PlatformFeature feature, Map<String, List<String>> dependencyMap) {
        return new PlatformFeatureResponse(
                feature.getKey(),
                feature.getName(),
                feature.getDescription(),
                feature.getValueType(),
                feature.getUnit(),
                feature.isActive(),
                feature.getImplementationStatus(),
                dependencyMap.getOrDefault(feature.getKey(), List.of())
        );
    }

    private SubscriptionPlanSummaryResponse toPlanSummaryResponse(SubscriptionPlan plan) {
        return new SubscriptionPlanSummaryResponse(
                plan.getId(),
                plan.getCode(),
                plan.getName(),
                plan.getDescription(),
                plan.getStatus(),
                plan.getDisplayOrder(),
                plan.isCustom()
        );
    }

    private PlanEntitlementResponse toPlanEntitlementResponse(PlatformFeature feature,
                                                              PlanEntitlement entitlement,
                                                              Map<String, List<String>> dependencyMap) {
        boolean enabled = entitlement != null && entitlement.isEnabled();
        Integer limitValue = entitlement == null ? null : entitlement.getLimitValue();

        if (feature.getValueType() == PlatformFeatureValueType.BOOLEAN && limitValue != null) {
            throw new IllegalStateException("BOOLEAN feature " + feature.getKey() + " must not define limitValue.");
        }
        if (feature.getValueType() == PlatformFeatureValueType.INTEGER_LIMIT && entitlement != null && !enabled) {
            throw new IllegalStateException("INTEGER_LIMIT feature " + feature.getKey() + " must stay enabled when present.");
        }

        return new PlanEntitlementResponse(
                feature.getKey(),
                feature.getName(),
                feature.getDescription(),
                feature.getValueType(),
                feature.getUnit(),
                feature.getImplementationStatus(),
                dependencyMap.getOrDefault(feature.getKey(), List.of()),
                enabled,
                limitValue
        );
    }

    private Map<String, List<String>> dependenciesByFeature(Collection<String> featureKeys) {
        if (featureKeys.isEmpty()) {
            return Map.of();
        }

        Map<String, List<String>> dependencyMap = new LinkedHashMap<>();
        for (PlatformFeatureDependency dependency : platformFeatureDependencyRepository.findAllByFeatureKeys(featureKeys)) {
            dependencyMap.computeIfAbsent(dependency.getFeature().getKey(), ignored -> new ArrayList<>())
                    .add(dependency.getRequiredFeature().getKey());
        }
        dependencyMap.replaceAll((ignored, dependencies) -> dependencies.stream().sorted().toList());
        return dependencyMap;
    }

    private void detectDependencyCycles(List<PlatformFeatureDependency> dependencies) {
        Map<String, List<String>> graph = new HashMap<>();
        for (PlatformFeatureDependency dependency : dependencies) {
            graph.computeIfAbsent(dependency.getFeature().getKey(), ignored -> new ArrayList<>())
                    .add(dependency.getRequiredFeature().getKey());
            graph.computeIfAbsent(dependency.getRequiredFeature().getKey(), ignored -> new ArrayList<>());
        }

        Set<String> visited = new HashSet<>();
        Set<String> visiting = new HashSet<>();
        Deque<String> trail = new ArrayDeque<>();
        for (String featureKey : graph.keySet()) {
            if (!visited.contains(featureKey)) {
                depthFirstValidate(featureKey, graph, visited, visiting, trail);
            }
        }
    }

    private void depthFirstValidate(String featureKey,
                                    Map<String, List<String>> graph,
                                    Set<String> visited,
                                    Set<String> visiting,
                                    Deque<String> trail) {
        visiting.add(featureKey);
        trail.push(featureKey);

        for (String dependencyKey : graph.getOrDefault(featureKey, List.of())) {
            if (visiting.contains(dependencyKey)) {
                List<String> cycle = new ArrayList<>(trail);
                cycle.add(0, dependencyKey);
                throw new IllegalStateException("Platform feature dependency cycle detected: " + cycle);
            }
            if (!visited.contains(dependencyKey)) {
                depthFirstValidate(dependencyKey, graph, visited, visiting, trail);
            }
        }

        trail.pop();
        visiting.remove(featureKey);
        visited.add(featureKey);
    }

    private String normalizeCatalogKey(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }
}
