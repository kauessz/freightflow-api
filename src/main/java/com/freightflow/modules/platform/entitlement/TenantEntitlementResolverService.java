package com.freightflow.modules.platform.entitlement;

import com.freightflow.modules.auth.Tenant;
import com.freightflow.modules.auth.TenantRepository;
import com.freightflow.modules.platform.catalog.PlanEntitlement;
import com.freightflow.modules.platform.catalog.PlatformFeature;
import com.freightflow.modules.platform.catalog.PlatformFeatureDependency;
import com.freightflow.modules.platform.catalog.PlatformFeatureDependencyRepository;
import com.freightflow.modules.platform.catalog.PlatformFeatureImplementationStatus;
import com.freightflow.modules.platform.catalog.PlatformFeatureRepository;
import com.freightflow.modules.platform.catalog.PlatformFeatureValueType;
import com.freightflow.modules.platform.catalog.SubscriptionPlan;
import com.freightflow.modules.platform.catalog.SubscriptionPlanRepository;
import com.freightflow.modules.platform.catalog.SubscriptionPlanStatus;
import com.freightflow.modules.platform.subscription.TenantSubscription;
import com.freightflow.modules.platform.subscription.TenantSubscriptionRepository;
import com.freightflow.modules.platform.subscription.TenantSubscriptionStatus;
import com.freightflow.shared.exception.BadRequestException;
import com.freightflow.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TenantEntitlementResolverService {

    private final TenantRepository tenantRepository;
    private final TenantSubscriptionRepository tenantSubscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final PlatformFeatureRepository platformFeatureRepository;
    private final PlatformFeatureDependencyRepository platformFeatureDependencyRepository;

    public TenantEntitlementResolverService(TenantRepository tenantRepository,
                                            TenantSubscriptionRepository tenantSubscriptionRepository,
                                            SubscriptionPlanRepository subscriptionPlanRepository,
                                            PlatformFeatureRepository platformFeatureRepository,
                                            PlatformFeatureDependencyRepository platformFeatureDependencyRepository) {
        this.tenantRepository = tenantRepository;
        this.tenantSubscriptionRepository = tenantSubscriptionRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.platformFeatureRepository = platformFeatureRepository;
        this.platformFeatureDependencyRepository = platformFeatureDependencyRepository;
    }

    public TenantEntitlementResolution resolveTenantEntitlements(UUID tenantId) {
        Tenant tenant = requireTenant(tenantId);
        Instant resolvedAt = Instant.now();
        List<PlatformFeature> catalog = platformFeatureRepository.findAll().stream()
                .sorted(Comparator.comparing(PlatformFeature::getKey))
                .toList();
        Map<String, PlatformFeature> featuresByKey = new LinkedHashMap<>();
        for (PlatformFeature feature : catalog) {
            featuresByKey.put(feature.getKey(), feature);
        }

        Map<String, List<String>> directDependencies = buildDirectDependencies(
                platformFeatureDependencyRepository.findAllWithRequiredFeature()
        );
        DependencyGraphAnalysis dependencyAnalysis = analyzeDependencies(featuresByKey.keySet(), directDependencies);

        List<TenantSubscription> openSubscriptions = tenantSubscriptionRepository
                .findAllByTenantIdAndEndedAtIsNullAndStatusInOrderByStartedAtDescCreatedAtDesc(
                        tenant.getId(),
                        List.of(TenantSubscriptionStatus.ACTIVE, TenantSubscriptionStatus.SUSPENDED)
                );

        LinkedHashSet<String> rootWarnings = new LinkedHashSet<>();
        boolean inconsistentSubscriptions = openSubscriptions.size() > 1;
        if (inconsistentSubscriptions) {
            rootWarnings.add("Multiple open subscriptions were found. Entitlements cannot be resolved safely.");
        }

        Optional<TenantSubscription> currentSubscription = inconsistentSubscriptions || openSubscriptions.isEmpty()
                ? Optional.empty()
                : Optional.of(openSubscriptions.getFirst());

        TenantEntitlementAccessStatus accessStatus = resolveAccessStatus(currentSubscription, inconsistentSubscriptions);
        SubscriptionPlan detailedPlan = accessStatus == TenantEntitlementAccessStatus.INCONSISTENT_SUBSCRIPTION
                ? null
                : currentSubscription
                .map(TenantSubscription::getPlan)
                .map(SubscriptionPlan::getId)
                .flatMap(subscriptionPlanRepository::findDetailedById)
                .orElse(null);

        if (currentSubscription.isPresent() && detailedPlan == null) {
            rootWarnings.add("Open subscription references a plan that could not be loaded; effective access was disabled conservatively.");
        }

        if (currentSubscription.isPresent() && currentSubscription.get().getStatus() == TenantSubscriptionStatus.SUSPENDED) {
            rootWarnings.add("Tenant subscription is suspended; all effective entitlements are disabled until reactivation.");
        }
        if (detailedPlan != null && detailedPlan.getStatus() != SubscriptionPlanStatus.ACTIVE) {
            rootWarnings.add("Open subscription references a non-active plan; effective entitlements were disabled conservatively.");
        }
        rootWarnings.addAll(dependencyAnalysis.globalWarnings());

        Map<String, PlanEntitlement> entitlementsByFeature = new HashMap<>();
        if (detailedPlan != null && accessStatus != TenantEntitlementAccessStatus.INCONSISTENT_SUBSCRIPTION) {
            for (PlanEntitlement entitlement : detailedPlan.getEntitlements()) {
                entitlementsByFeature.put(entitlement.getFeature().getKey(), entitlement);
            }
        }

        EvaluationContext context = new EvaluationContext(
                currentSubscription.orElse(null),
                detailedPlan,
                accessStatus,
                featuresByKey,
                entitlementsByFeature,
                directDependencies,
                dependencyAnalysis
        );

        List<ResolvedFeatureEntitlement> resolvedFeatures = catalog.stream()
                .map(feature -> evaluateFeature(feature.getKey(), context, new ArrayDeque<>()))
                .toList();

        ResolvedTenantSubscription subscription = currentSubscription.isPresent() && detailedPlan != null
                ? new ResolvedTenantSubscription(
                        currentSubscription.get().getId(),
                        currentSubscription.get().getStatus(),
                        detailedPlan.getId(),
                        detailedPlan.getCode(),
                        detailedPlan.getStatus(),
                        currentSubscription.get().getStartedAt(),
                        currentSubscription.get().getEndedAt()
                )
                : null;

        return new TenantEntitlementResolution(
                tenant.getId(),
                accessStatus,
                subscription,
                resolvedFeatures,
                sortDistinct(rootWarnings),
                resolvedAt
        );
    }

    public ResolvedFeatureEntitlement resolveFeatureEntitlement(UUID tenantId, String rawFeatureKey) {
        String featureKey = normalizeFeatureKey(rawFeatureKey);
        TenantEntitlementResolution resolution = resolveTenantEntitlements(tenantId);
        return resolution.features().stream()
                .filter(feature -> feature.featureKey().equals(featureKey))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Platform feature", featureKey));
    }

    public boolean hasEffectiveFeature(UUID tenantId, String rawFeatureKey) {
        return resolveFeatureEntitlement(tenantId, rawFeatureKey).effectiveEnabled();
    }

    public Integer resolveIntegerLimit(UUID tenantId, String rawFeatureKey) {
        ResolvedFeatureEntitlement feature = resolveFeatureEntitlement(tenantId, rawFeatureKey);
        if (feature.valueType() != PlatformFeatureValueType.INTEGER_LIMIT) {
            throw new BadRequestException("Feature '%s' is not an INTEGER_LIMIT entitlement.".formatted(feature.featureKey()));
        }
        return feature.effectiveEnabled() ? feature.limitValue() : null;
    }

    private Tenant requireTenant(UUID tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId));
    }

    private String normalizeFeatureKey(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new BadRequestException("Parameter 'featureKey' must not be blank.");
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private TenantEntitlementAccessStatus resolveAccessStatus(Optional<TenantSubscription> currentSubscription,
                                                              boolean inconsistentSubscriptions) {
        if (inconsistentSubscriptions) {
            return TenantEntitlementAccessStatus.INCONSISTENT_SUBSCRIPTION;
        }
        if (currentSubscription.isEmpty()) {
            return TenantEntitlementAccessStatus.NO_SUBSCRIPTION;
        }
        return currentSubscription.get().getStatus() == TenantSubscriptionStatus.SUSPENDED
                ? TenantEntitlementAccessStatus.SUSPENDED
                : TenantEntitlementAccessStatus.ACTIVE;
    }

    private Map<String, List<String>> buildDirectDependencies(List<PlatformFeatureDependency> dependencies) {
        Map<String, List<String>> directDependencies = new LinkedHashMap<>();
        for (PlatformFeatureDependency dependency : dependencies) {
            directDependencies.computeIfAbsent(dependency.getFeature().getKey(), ignored -> new ArrayList<>())
                    .add(dependency.getRequiredFeature().getKey());
        }
        directDependencies.replaceAll((ignored, values) -> values.stream().distinct().sorted().toList());
        return directDependencies;
    }

    private DependencyGraphAnalysis analyzeDependencies(Collection<String> featureKeys,
                                                        Map<String, List<String>> directDependencies) {
        Map<String, Set<String>> closures = new HashMap<>();
        Set<String> cycleAffected = new HashSet<>();
        LinkedHashSet<String> warnings = new LinkedHashSet<>();

        for (String featureKey : featureKeys) {
            computeClosure(featureKey, directDependencies, closures, cycleAffected, warnings, new ArrayDeque<>());
        }

        Map<String, List<String>> normalizedClosures = new LinkedHashMap<>();
        for (String featureKey : featureKeys.stream().sorted().toList()) {
            normalizedClosures.put(featureKey, closures.getOrDefault(featureKey, Set.of()).stream().sorted().toList());
        }
        return new DependencyGraphAnalysis(normalizedClosures, cycleAffected, sortDistinct(warnings));
    }

    private Set<String> computeClosure(String featureKey,
                                       Map<String, List<String>> directDependencies,
                                       Map<String, Set<String>> closures,
                                       Set<String> cycleAffected,
                                       LinkedHashSet<String> warnings,
                                       Deque<String> stack) {
        if (closures.containsKey(featureKey)) {
            return closures.get(featureKey);
        }
        if (stack.contains(featureKey)) {
            List<String> cycle = new ArrayList<>();
            boolean collect = false;
            for (String item : stack) {
                if (Objects.equals(item, featureKey)) {
                    collect = true;
                }
                if (collect) {
                    cycle.add(item);
                }
            }
            cycle.add(featureKey);
            cycleAffected.addAll(cycle);
            warnings.add("Dependency cycle detected involving: " + cycle.stream().distinct().sorted().reduce((a, b) -> a + ", " + b).orElse(featureKey));
            return Set.of();
        }

        stack.addLast(featureKey);
        Set<String> closure = new TreeSet<>();
        for (String dependencyKey : directDependencies.getOrDefault(featureKey, List.of())) {
            closure.add(dependencyKey);
            closure.addAll(computeClosure(dependencyKey, directDependencies, closures, cycleAffected, warnings, stack));
        }
        stack.removeLast();
        closures.put(featureKey, closure);
        return closure;
    }

    private ResolvedFeatureEntitlement evaluateFeature(String featureKey,
                                                       EvaluationContext context,
                                                       Deque<String> evaluationStack) {
        ResolvedFeatureEntitlement cached = context.resolvedFeatures().get(featureKey);
        if (cached != null) {
            return cached;
        }

        PlatformFeature feature = context.featuresByKey().get(featureKey);
        if (feature == null) {
            throw new ResourceNotFoundException("Platform feature", featureKey);
        }

        if (evaluationStack.contains(featureKey)) {
            List<String> warnings = List.of("Dependency cycle detected while evaluating this feature.");
            ResolvedFeatureEntitlement response = new ResolvedFeatureEntitlement(
                    feature.getKey(),
                    feature.getName(),
                    feature.getValueType(),
                    feature.getImplementationStatus(),
                    feature.isActive(),
                    false,
                    false,
                    null,
                    false,
                    context.dependencyAnalysis().dependencyClosure().getOrDefault(featureKey, List.of()),
                    context.directDependencies().getOrDefault(featureKey, List.of()),
                    warnings
            );
            context.resolvedFeatures().put(featureKey, response);
            return response;
        }

        evaluationStack.addLast(featureKey);
        PlanEntitlement entitlement = context.entitlementsByFeature().get(featureKey);
        LinkedHashSet<String> warnings = new LinkedHashSet<>();
        List<String> dependencies = context.dependencyAnalysis().dependencyClosure().getOrDefault(featureKey, List.of());
        TreeSet<String> unmetDependencies = new TreeSet<>();

        boolean grantedByPlan = entitlement != null && entitlement.isEnabled();
        Integer limitValue = null;
        boolean unlimited = false;
        boolean dataConsistent = true;

        if (feature.getValueType() == PlatformFeatureValueType.BOOLEAN) {
            if (entitlement != null && entitlement.getLimitValue() != null) {
                warnings.add("BOOLEAN feature has an unexpected limitValue; effective access was disabled conservatively.");
                dataConsistent = false;
            }
        } else if (entitlement != null && entitlement.isEnabled()) {
            limitValue = entitlement.getLimitValue();
            unlimited = limitValue == null;
        } else if (entitlement != null && entitlement.getLimitValue() != null) {
            warnings.add("Disabled INTEGER_LIMIT entitlement has an unexpected limitValue; effective access was disabled conservatively.");
            dataConsistent = false;
        }

        if (feature.getImplementationStatus() == PlatformFeatureImplementationStatus.PARTIAL) {
            warnings.add("Feature implementation is PARTIAL.");
        }
        if (!feature.isActive()) {
            warnings.add("Feature is inactive in the catalog.");
        }
        if (feature.getImplementationStatus() == PlatformFeatureImplementationStatus.PLANNED) {
            warnings.add("Feature is still PLANNED and cannot be effectively enabled.");
        }
        if (context.accessStatus() == TenantEntitlementAccessStatus.INCONSISTENT_SUBSCRIPTION) {
            grantedByPlan = false;
            limitValue = null;
            unlimited = false;
        }
        if (context.subscription() == null) {
            grantedByPlan = false;
            limitValue = null;
            unlimited = false;
        }
        if (context.subscription() != null && context.subscription().getStatus() == TenantSubscriptionStatus.SUSPENDED) {
            warnings.add("Subscription is suspended.");
        }
        if (context.plan() != null && context.plan().getStatus() != SubscriptionPlanStatus.ACTIVE) {
            warnings.add("Plan status is %s, so effective access is disabled conservatively.".formatted(context.plan().getStatus()));
        }
        if (context.dependencyAnalysis().cycleAffected().contains(featureKey)) {
            warnings.add("Feature is part of a dependency cycle.");
            dataConsistent = false;
        }

        boolean effectiveEnabled = grantedByPlan
                && context.accessStatus() == TenantEntitlementAccessStatus.ACTIVE
                && context.plan() != null
                && context.plan().getStatus() == SubscriptionPlanStatus.ACTIVE
                && feature.isActive()
                && feature.getImplementationStatus() != PlatformFeatureImplementationStatus.PLANNED
                && dataConsistent;

        for (String dependencyKey : context.directDependencies().getOrDefault(featureKey, List.of())) {
            PlatformFeature dependency = context.featuresByKey().get(dependencyKey);
            if (dependency == null) {
                unmetDependencies.add(dependencyKey);
                warnings.add("Dependency '%s' does not exist in the catalog.".formatted(dependencyKey));
                effectiveEnabled = false;
                continue;
            }
            ResolvedFeatureEntitlement dependencyEvaluation = evaluateFeature(dependencyKey, context, evaluationStack);
            if (!dependencyEvaluation.effectiveEnabled()) {
                unmetDependencies.add(dependencyKey);
                warnings.add("Dependency '%s' is not effectively enabled.".formatted(dependencyKey));
                effectiveEnabled = false;
            }
        }

        if (!grantedByPlan) {
            limitValue = null;
            unlimited = false;
        }
        if (!effectiveEnabled) {
            unlimited = false;
        }

        ResolvedFeatureEntitlement response = new ResolvedFeatureEntitlement(
                feature.getKey(),
                feature.getName(),
                feature.getValueType(),
                feature.getImplementationStatus(),
                feature.isActive(),
                grantedByPlan,
                effectiveEnabled,
                feature.getValueType() == PlatformFeatureValueType.BOOLEAN ? null : limitValue,
                feature.getValueType() == PlatformFeatureValueType.INTEGER_LIMIT
                        && grantedByPlan
                        && effectiveEnabled
                        && unlimited,
                dependencies,
                List.copyOf(unmetDependencies),
                sortDistinct(warnings)
        );
        context.resolvedFeatures().put(featureKey, response);
        evaluationStack.removeLast();
        return response;
    }

    private List<String> sortDistinct(Collection<String> values) {
        return values.stream()
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();
    }

    private record DependencyGraphAnalysis(
            Map<String, List<String>> dependencyClosure,
            Set<String> cycleAffected,
            List<String> globalWarnings
    ) {
    }

    private static final class EvaluationContext {
        private final TenantSubscription subscription;
        private final SubscriptionPlan plan;
        private final TenantEntitlementAccessStatus accessStatus;
        private final Map<String, PlatformFeature> featuresByKey;
        private final Map<String, PlanEntitlement> entitlementsByFeature;
        private final Map<String, List<String>> directDependencies;
        private final DependencyGraphAnalysis dependencyAnalysis;
        private final Map<String, ResolvedFeatureEntitlement> resolvedFeatures = new HashMap<>();

        private EvaluationContext(TenantSubscription subscription,
                                  SubscriptionPlan plan,
                                  TenantEntitlementAccessStatus accessStatus,
                                  Map<String, PlatformFeature> featuresByKey,
                                  Map<String, PlanEntitlement> entitlementsByFeature,
                                  Map<String, List<String>> directDependencies,
                                  DependencyGraphAnalysis dependencyAnalysis) {
            this.subscription = subscription;
            this.plan = plan;
            this.accessStatus = accessStatus;
            this.featuresByKey = featuresByKey;
            this.entitlementsByFeature = entitlementsByFeature;
            this.directDependencies = directDependencies;
            this.dependencyAnalysis = dependencyAnalysis;
        }

        private TenantSubscription subscription() {
            return subscription;
        }

        private SubscriptionPlan plan() {
            return plan;
        }

        private TenantEntitlementAccessStatus accessStatus() {
            return accessStatus;
        }

        private Map<String, PlatformFeature> featuresByKey() {
            return featuresByKey;
        }

        private Map<String, PlanEntitlement> entitlementsByFeature() {
            return entitlementsByFeature;
        }

        private Map<String, List<String>> directDependencies() {
            return directDependencies;
        }

        private DependencyGraphAnalysis dependencyAnalysis() {
            return dependencyAnalysis;
        }

        private Map<String, ResolvedFeatureEntitlement> resolvedFeatures() {
            return resolvedFeatures;
        }
    }
}
