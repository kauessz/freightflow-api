package com.freightflow.modules.platform.entitlement;

import com.freightflow.modules.auth.Tenant;
import com.freightflow.modules.auth.TenantRepository;
import com.freightflow.modules.platform.catalog.PlanEntitlement;
import com.freightflow.modules.platform.catalog.PlanEntitlementId;
import com.freightflow.modules.platform.catalog.PlatformFeature;
import com.freightflow.modules.platform.catalog.PlatformFeatureDependency;
import com.freightflow.modules.platform.catalog.PlatformFeatureDependencyId;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TenantEntitlementResolverService")
class TenantEntitlementResolverServiceTest {

    @Mock private TenantRepository tenantRepository;
    @Mock private TenantSubscriptionRepository tenantSubscriptionRepository;
    @Mock private SubscriptionPlanRepository subscriptionPlanRepository;
    @Mock private PlatformFeatureRepository platformFeatureRepository;
    @Mock private PlatformFeatureDependencyRepository platformFeatureDependencyRepository;

    @InjectMocks private TenantEntitlementResolverService service;

    @Test
    @DisplayName("tenant inexistente retorna not found")
    void tenantInexistenteRetornaNotFound() {
        UUID tenantId = UUID.randomUUID();
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resolveTenantEntitlements(tenantId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Tenant");
    }

    @Test
    @DisplayName("tenant sem assinatura retorna no subscription com catalogo completo")
    void tenantSemAssinaturaRetornaNoSubscriptionComCatalogoCompleto() {
        Tenant tenant = tenant();
        PlatformFeature tracking = feature("TRACKING", PlatformFeatureValueType.BOOLEAN, true, PlatformFeatureImplementationStatus.AVAILABLE);
        PlatformFeature fleetMap = feature("FLEET_MAP", PlatformFeatureValueType.BOOLEAN, true, PlatformFeatureImplementationStatus.AVAILABLE);

        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
        when(platformFeatureRepository.findAll()).thenReturn(List.of(fleetMap, tracking));
        when(platformFeatureDependencyRepository.findAllWithRequiredFeature()).thenReturn(List.of(dependency(fleetMap, tracking)));
        when(tenantSubscriptionRepository.findAllByTenantIdAndEndedAtIsNullAndStatusInOrderByStartedAtDescCreatedAtDesc(
                tenant.getId(), List.of(TenantSubscriptionStatus.ACTIVE, TenantSubscriptionStatus.SUSPENDED)
        )).thenReturn(List.of());

        TenantEntitlementResolution resolution = service.resolveTenantEntitlements(tenant.getId());

        assertThat(resolution.accessStatus()).isEqualTo(TenantEntitlementAccessStatus.NO_SUBSCRIPTION);
        assertThat(resolution.subscription()).isNull();
        assertThat(resolution.features()).hasSize(2);
        assertThat(resolution.features()).allSatisfy(feature -> {
            assertThat(feature.grantedByPlan()).isFalse();
            assertThat(feature.effectiveEnabled()).isFalse();
            assertThat(feature.unlimited()).isFalse();
        });
        assertThat(resolution.features())
                .filteredOn(item -> item.featureKey().equals("FLEET_MAP"))
                .singleElement()
                .satisfies(item -> assertThat(item.dependencies()).containsExactly("TRACKING"));
    }

    @Test
    @DisplayName("multiplas assinaturas abertas retornam inconsistent subscription sem escolher plano")
    void multiplasAssinaturasAbertasRetornamInconsistentSubscriptionSemEscolherPlano() {
        Tenant tenant = tenant();
        PlatformFeature tracking = feature("TRACKING", PlatformFeatureValueType.BOOLEAN, true, PlatformFeatureImplementationStatus.AVAILABLE);
        PlatformFeature maxApi = feature("MAX_MONTHLY_API_REQUESTS", PlatformFeatureValueType.INTEGER_LIMIT, true, PlatformFeatureImplementationStatus.AVAILABLE);
        SubscriptionPlan professional = plan(UUID.randomUUID(), "PROFESSIONAL", SubscriptionPlanStatus.ACTIVE);
        SubscriptionPlan enterprise = plan(UUID.randomUUID(), "ENTERPRISE", SubscriptionPlanStatus.ACTIVE);
        TenantSubscription active = subscription(tenant, professional, TenantSubscriptionStatus.ACTIVE);
        TenantSubscription suspended = subscription(tenant, enterprise, TenantSubscriptionStatus.SUSPENDED);

        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
        when(platformFeatureRepository.findAll()).thenReturn(List.of(maxApi, tracking));
        when(platformFeatureDependencyRepository.findAllWithRequiredFeature()).thenReturn(List.of());
        when(tenantSubscriptionRepository.findAllByTenantIdAndEndedAtIsNullAndStatusInOrderByStartedAtDescCreatedAtDesc(
                tenant.getId(), List.of(TenantSubscriptionStatus.ACTIVE, TenantSubscriptionStatus.SUSPENDED)
        )).thenReturn(List.of(active, suspended));

        TenantEntitlementResolution resolution = service.resolveTenantEntitlements(tenant.getId());

        assertThat(resolution.accessStatus()).isEqualTo(TenantEntitlementAccessStatus.INCONSISTENT_SUBSCRIPTION);
        assertThat(resolution.subscription()).isNull();
        assertThat(resolution.warnings()).containsExactly("Multiple open subscriptions were found. Entitlements cannot be resolved safely.");
        assertThat(resolution.features()).allSatisfy(feature -> {
            assertThat(feature.grantedByPlan()).isFalse();
            assertThat(feature.effectiveEnabled()).isFalse();
            assertThat(feature.limitValue()).isNull();
            assertThat(feature.unlimited()).isFalse();
        });
    }

    @Test
    @DisplayName("assinatura active resolve boolean limite finito ilimitado e dependencias sem n mais um")
    void assinaturaActiveResolveBooleanLimiteFinitoIlimitadoEDependenciasSemNMaisUm() {
        Tenant tenant = tenant();
        PlatformFeature tracking = feature("TRACKING", PlatformFeatureValueType.BOOLEAN, true, PlatformFeatureImplementationStatus.AVAILABLE);
        PlatformFeature fleetMap = feature("FLEET_MAP", PlatformFeatureValueType.BOOLEAN, true, PlatformFeatureImplementationStatus.AVAILABLE);
        PlatformFeature maxUsers = feature("MAX_ACTIVE_USERS", PlatformFeatureValueType.INTEGER_LIMIT, true, PlatformFeatureImplementationStatus.AVAILABLE);
        PlatformFeature maxApi = feature("MAX_MONTHLY_API_REQUESTS", PlatformFeatureValueType.INTEGER_LIMIT, true, PlatformFeatureImplementationStatus.AVAILABLE);
        SubscriptionPlan plan = plan(UUID.randomUUID(), "ENTERPRISE", SubscriptionPlanStatus.ACTIVE);
        setField(plan, "entitlements", Set.of(
                entitlement(plan, tracking, true, null),
                entitlement(plan, fleetMap, true, null),
                entitlement(plan, maxUsers, true, 25),
                entitlement(plan, maxApi, true, null)
        ));
        TenantSubscription subscription = subscription(tenant, plan, TenantSubscriptionStatus.ACTIVE);

        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
        when(platformFeatureRepository.findAll()).thenReturn(List.of(maxApi, tracking, fleetMap, maxUsers));
        when(platformFeatureDependencyRepository.findAllWithRequiredFeature()).thenReturn(List.of(dependency(fleetMap, tracking)));
        when(tenantSubscriptionRepository.findAllByTenantIdAndEndedAtIsNullAndStatusInOrderByStartedAtDescCreatedAtDesc(
                tenant.getId(), List.of(TenantSubscriptionStatus.ACTIVE, TenantSubscriptionStatus.SUSPENDED)
        )).thenReturn(List.of(subscription));
        when(subscriptionPlanRepository.findDetailedById(plan.getId())).thenReturn(Optional.of(plan));

        TenantEntitlementResolution resolution = service.resolveTenantEntitlements(tenant.getId());

        assertThat(resolution.accessStatus()).isEqualTo(TenantEntitlementAccessStatus.ACTIVE);
        assertThat(resolution.subscription()).isNotNull();
        assertFeature(resolution, "TRACKING", true, true);
        assertFeature(resolution, "FLEET_MAP", true, true);
        assertThat(findFeature(resolution, "FLEET_MAP").dependencies()).containsExactly("TRACKING");
        assertThat(findFeature(resolution, "MAX_ACTIVE_USERS").limitValue()).isEqualTo(25);
        assertThat(findFeature(resolution, "MAX_ACTIVE_USERS").unlimited()).isFalse();
        assertThat(findFeature(resolution, "MAX_MONTHLY_API_REQUESTS").limitValue()).isNull();
        assertThat(findFeature(resolution, "MAX_MONTHLY_API_REQUESTS").unlimited()).isTrue();
        assertThat(service.hasEffectiveFeature(tenant.getId(), "tracking")).isTrue();
        assertThat(service.resolveIntegerLimit(tenant.getId(), "MAX_ACTIVE_USERS")).isEqualTo(25);
        assertThat(service.resolveIntegerLimit(tenant.getId(), "MAX_MONTHLY_API_REQUESTS")).isNull();

        verify(tenantRepository, times(4)).findById(tenant.getId());
        verify(platformFeatureRepository, times(4)).findAll();
        verify(platformFeatureDependencyRepository, times(4)).findAllWithRequiredFeature();
        verify(tenantSubscriptionRepository, times(4))
                .findAllByTenantIdAndEndedAtIsNullAndStatusInOrderByStartedAtDescCreatedAtDesc(
                        tenant.getId(), List.of(TenantSubscriptionStatus.ACTIVE, TenantSubscriptionStatus.SUSPENDED)
                );
        verify(subscriptionPlanRepository, times(4)).findDetailedById(plan.getId());
    }

    @Test
    @DisplayName("assinatura suspended preserva grant mas desabilita efetividade")
    void assinaturaSuspendedPreservaGrantMasDesabilitaEfetividade() {
        Tenant tenant = tenant();
        PlatformFeature tracking = feature("TRACKING", PlatformFeatureValueType.BOOLEAN, true, PlatformFeatureImplementationStatus.AVAILABLE);
        PlatformFeature maxApi = feature("MAX_MONTHLY_API_REQUESTS", PlatformFeatureValueType.INTEGER_LIMIT, true, PlatformFeatureImplementationStatus.AVAILABLE);
        SubscriptionPlan plan = plan(UUID.randomUUID(), "PROFESSIONAL", SubscriptionPlanStatus.ACTIVE);
        setField(plan, "entitlements", Set.of(
                entitlement(plan, tracking, true, null),
                entitlement(plan, maxApi, true, null)
        ));
        TenantSubscription subscription = subscription(tenant, plan, TenantSubscriptionStatus.SUSPENDED);

        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
        when(platformFeatureRepository.findAll()).thenReturn(List.of(maxApi, tracking));
        when(platformFeatureDependencyRepository.findAllWithRequiredFeature()).thenReturn(List.of());
        when(tenantSubscriptionRepository.findAllByTenantIdAndEndedAtIsNullAndStatusInOrderByStartedAtDescCreatedAtDesc(
                tenant.getId(), List.of(TenantSubscriptionStatus.ACTIVE, TenantSubscriptionStatus.SUSPENDED)
        )).thenReturn(List.of(subscription));
        when(subscriptionPlanRepository.findDetailedById(plan.getId())).thenReturn(Optional.of(plan));

        TenantEntitlementResolution resolution = service.resolveTenantEntitlements(tenant.getId());

        assertThat(resolution.accessStatus()).isEqualTo(TenantEntitlementAccessStatus.SUSPENDED);
        assertFeature(resolution, "TRACKING", true, false);
        assertThat(findFeature(resolution, "TRACKING").warnings()).contains("Subscription is suspended.");
        assertThat(findFeature(resolution, "MAX_MONTHLY_API_REQUESTS").grantedByPlan()).isTrue();
        assertThat(findFeature(resolution, "MAX_MONTHLY_API_REQUESTS").effectiveEnabled()).isFalse();
        assertThat(findFeature(resolution, "MAX_MONTHLY_API_REQUESTS").limitValue()).isNull();
        assertThat(findFeature(resolution, "MAX_MONTHLY_API_REQUESTS").unlimited()).isFalse();
        assertThat(resolution.warnings()).anyMatch(item -> item.contains("suspended"));
    }

    @Test
    @DisplayName("entitlement desabilitado ou ausente nao concede feature")
    void entitlementDesabilitadoOuAusenteNaoConcedeFeature() {
        Tenant tenant = tenant();
        PlatformFeature tracking = feature("TRACKING", PlatformFeatureValueType.BOOLEAN, true, PlatformFeatureImplementationStatus.AVAILABLE);
        PlatformFeature fleetMap = feature("FLEET_MAP", PlatformFeatureValueType.BOOLEAN, true, PlatformFeatureImplementationStatus.AVAILABLE);
        SubscriptionPlan plan = plan(UUID.randomUUID(), "PROFESSIONAL", SubscriptionPlanStatus.ACTIVE);
        setField(plan, "entitlements", Set.of(entitlement(plan, tracking, false, null)));
        TenantSubscription subscription = subscription(tenant, plan, TenantSubscriptionStatus.ACTIVE);

        stubActiveResolution(tenant, List.of(fleetMap, tracking), List.of(), subscription, plan);

        TenantEntitlementResolution resolution = service.resolveTenantEntitlements(tenant.getId());

        assertFeature(resolution, "TRACKING", false, false);
        assertFeature(resolution, "FLEET_MAP", false, false);
    }

    @Test
    @DisplayName("feature partial planned e inactive recebem tratamento diagnostico conservador")
    void featurePartialPlannedEInactiveRecebemTratamentoDiagnosticoConservador() {
        Tenant tenant = tenant();
        PlatformFeature partial = feature("CLIENT_PORTAL", PlatformFeatureValueType.BOOLEAN, true, PlatformFeatureImplementationStatus.PARTIAL);
        PlatformFeature planned = feature("BOOKING_MANAGEMENT", PlatformFeatureValueType.BOOLEAN, true, PlatformFeatureImplementationStatus.PLANNED);
        PlatformFeature inactive = feature("API_ACCESS", PlatformFeatureValueType.BOOLEAN, false, PlatformFeatureImplementationStatus.AVAILABLE);
        SubscriptionPlan plan = plan(UUID.randomUUID(), "ENTERPRISE", SubscriptionPlanStatus.ACTIVE);
        setField(plan, "entitlements", Set.of(
                entitlement(plan, partial, true, null),
                entitlement(plan, planned, true, null),
                entitlement(plan, inactive, true, null)
        ));
        TenantSubscription subscription = subscription(tenant, plan, TenantSubscriptionStatus.ACTIVE);

        stubActiveResolution(tenant, List.of(partial, planned, inactive), List.of(), subscription, plan);

        TenantEntitlementResolution resolution = service.resolveTenantEntitlements(tenant.getId());

        assertFeature(resolution, "CLIENT_PORTAL", true, true);
        assertThat(findFeature(resolution, "CLIENT_PORTAL").warnings()).contains("Feature implementation is PARTIAL.");
        assertFeature(resolution, "BOOKING_MANAGEMENT", true, false);
        assertThat(findFeature(resolution, "BOOKING_MANAGEMENT").warnings())
                .contains("Feature is still PLANNED and cannot be effectively enabled.");
        assertFeature(resolution, "API_ACCESS", true, false);
        assertThat(findFeature(resolution, "API_ACCESS").warnings()).contains("Feature is inactive in the catalog.");
    }

    @Test
    @DisplayName("dependencias direta transitive e ciclo sao resolvidas conservadoramente")
    void dependenciasDiretaTransitiveECicloSaoResolvidasConservadoramente() {
        Tenant tenant = tenant();
        PlatformFeature root = feature("ROOT", PlatformFeatureValueType.BOOLEAN, true, PlatformFeatureImplementationStatus.AVAILABLE);
        PlatformFeature middle = feature("MIDDLE", PlatformFeatureValueType.BOOLEAN, true, PlatformFeatureImplementationStatus.AVAILABLE);
        PlatformFeature leaf = feature("LEAF", PlatformFeatureValueType.BOOLEAN, true, PlatformFeatureImplementationStatus.AVAILABLE);
        PlatformFeature cycleA = feature("CYCLE_A", PlatformFeatureValueType.BOOLEAN, true, PlatformFeatureImplementationStatus.AVAILABLE);
        PlatformFeature cycleB = feature("CYCLE_B", PlatformFeatureValueType.BOOLEAN, true, PlatformFeatureImplementationStatus.AVAILABLE);
        SubscriptionPlan plan = plan(UUID.randomUUID(), "PROFESSIONAL", SubscriptionPlanStatus.ACTIVE);
        setField(plan, "entitlements", Set.of(
                entitlement(plan, root, true, null),
                entitlement(plan, middle, true, null),
                entitlement(plan, cycleA, true, null),
                entitlement(plan, cycleB, true, null)
        ));
        TenantSubscription subscription = subscription(tenant, plan, TenantSubscriptionStatus.ACTIVE);

        stubActiveResolution(
                tenant,
                List.of(cycleB, root, middle, leaf, cycleA),
                List.of(
                        dependency(root, middle),
                        dependency(middle, leaf),
                        dependency(cycleA, cycleB),
                        dependency(cycleB, cycleA)
                ),
                subscription,
                plan
        );

        TenantEntitlementResolution resolution = service.resolveTenantEntitlements(tenant.getId());

        assertFeature(resolution, "ROOT", true, false);
        assertThat(findFeature(resolution, "ROOT").dependencies()).containsExactly("LEAF", "MIDDLE");
        assertThat(findFeature(resolution, "ROOT").unmetDependencies()).containsExactly("MIDDLE");
        assertFeature(resolution, "MIDDLE", true, false);
        assertThat(findFeature(resolution, "MIDDLE").unmetDependencies()).containsExactly("LEAF");
        assertFeature(resolution, "CYCLE_A", true, false);
        assertFeature(resolution, "CYCLE_B", true, false);
        assertThat(findFeature(resolution, "CYCLE_A").warnings()).anyMatch(item -> item.contains("cycle"));
        assertThat(resolution.warnings()).anyMatch(item -> item.contains("Dependency cycle detected"));
    }

    @Test
    @DisplayName("plano draft associado a assinatura aberta desabilita efetividade")
    void planoDraftAssociadoAAssinaturaAbertaDesabilitaEfetividade() {
        Tenant tenant = tenant();
        PlatformFeature tracking = feature("TRACKING", PlatformFeatureValueType.BOOLEAN, true, PlatformFeatureImplementationStatus.AVAILABLE);
        PlatformFeature maxApi = feature("MAX_MONTHLY_API_REQUESTS", PlatformFeatureValueType.INTEGER_LIMIT, true, PlatformFeatureImplementationStatus.AVAILABLE);
        SubscriptionPlan plan = plan(UUID.randomUUID(), "CUSTOM", SubscriptionPlanStatus.DRAFT);
        setField(plan, "entitlements", Set.of(
                entitlement(plan, tracking, true, null),
                entitlement(plan, maxApi, true, null)
        ));
        TenantSubscription subscription = subscription(tenant, plan, TenantSubscriptionStatus.ACTIVE);

        stubActiveResolution(tenant, List.of(maxApi, tracking), List.of(), subscription, plan);

        TenantEntitlementResolution resolution = service.resolveTenantEntitlements(tenant.getId());

        assertFeature(resolution, "TRACKING", true, false);
        assertThat(findFeature(resolution, "MAX_MONTHLY_API_REQUESTS").grantedByPlan()).isTrue();
        assertThat(findFeature(resolution, "MAX_MONTHLY_API_REQUESTS").effectiveEnabled()).isFalse();
        assertThat(findFeature(resolution, "MAX_MONTHLY_API_REQUESTS").unlimited()).isFalse();
        assertThat(findFeature(resolution, "TRACKING").warnings())
                .anyMatch(item -> item.contains("Plan status is DRAFT"));
        assertThat(resolution.warnings())
                .anyMatch(item -> item.contains("non-active plan"));
    }

    @Test
    @DisplayName("plano archived preserva concessao e desabilita efetividade")
    void planoArchivedPreservaConcessaoEDesabilitaEfetividade() {
        Tenant tenant = tenant();
        PlatformFeature maxApi = feature("MAX_MONTHLY_API_REQUESTS", PlatformFeatureValueType.INTEGER_LIMIT, true, PlatformFeatureImplementationStatus.AVAILABLE);
        SubscriptionPlan plan = plan(UUID.randomUUID(), "LEGACY", SubscriptionPlanStatus.ARCHIVED);
        setField(plan, "entitlements", Set.of(entitlement(plan, maxApi, true, null)));
        TenantSubscription subscription = subscription(tenant, plan, TenantSubscriptionStatus.ACTIVE);

        stubActiveResolution(tenant, List.of(maxApi), List.of(), subscription, plan);

        TenantEntitlementResolution resolution = service.resolveTenantEntitlements(tenant.getId());

        assertThat(resolution.accessStatus()).isEqualTo(TenantEntitlementAccessStatus.ACTIVE);
        assertThat(findFeature(resolution, "MAX_MONTHLY_API_REQUESTS").grantedByPlan()).isTrue();
        assertThat(findFeature(resolution, "MAX_MONTHLY_API_REQUESTS").effectiveEnabled()).isFalse();
        assertThat(findFeature(resolution, "MAX_MONTHLY_API_REQUESTS").limitValue()).isNull();
        assertThat(findFeature(resolution, "MAX_MONTHLY_API_REQUESTS").unlimited()).isFalse();
        assertThat(findFeature(resolution, "MAX_MONTHLY_API_REQUESTS").warnings())
                .anyMatch(item -> item.contains("Plan status is ARCHIVED"));
    }

    @Test
    @DisplayName("dependencia para feature inativa ou planned nao apaga concessao da dependente")
    void dependenciaParaFeatureInativaOuPlannedNaoApagaConcessaoDaDependente() {
        Tenant tenant = tenant();
        PlatformFeature root = feature("ROOT", PlatformFeatureValueType.BOOLEAN, true, PlatformFeatureImplementationStatus.AVAILABLE);
        PlatformFeature inactiveDependency = feature("INACTIVE_DEP", PlatformFeatureValueType.BOOLEAN, false, PlatformFeatureImplementationStatus.AVAILABLE);
        PlatformFeature plannedDependency = feature("PLANNED_DEP", PlatformFeatureValueType.BOOLEAN, true, PlatformFeatureImplementationStatus.PLANNED);
        SubscriptionPlan plan = plan(UUID.randomUUID(), "PROFESSIONAL", SubscriptionPlanStatus.ACTIVE);
        setField(plan, "entitlements", Set.of(
                entitlement(plan, root, true, null),
                entitlement(plan, inactiveDependency, true, null),
                entitlement(plan, plannedDependency, true, null)
        ));
        TenantSubscription subscription = subscription(tenant, plan, TenantSubscriptionStatus.ACTIVE);

        stubActiveResolution(
                tenant,
                List.of(root, inactiveDependency, plannedDependency),
                List.of(
                        dependency(root, inactiveDependency),
                        dependency(inactiveDependency, plannedDependency)
                ),
                subscription,
                plan
        );

        TenantEntitlementResolution resolution = service.resolveTenantEntitlements(tenant.getId());

        assertThat(findFeature(resolution, "ROOT").grantedByPlan()).isTrue();
        assertThat(findFeature(resolution, "ROOT").effectiveEnabled()).isFalse();
        assertThat(findFeature(resolution, "ROOT").unmetDependencies()).containsExactly("INACTIVE_DEP");
        assertThat(findFeature(resolution, "INACTIVE_DEP").grantedByPlan()).isTrue();
        assertThat(findFeature(resolution, "INACTIVE_DEP").effectiveEnabled()).isFalse();
        assertThat(findFeature(resolution, "INACTIVE_DEP").warnings()).contains("Feature is inactive in the catalog.");
        assertThat(findFeature(resolution, "PLANNED_DEP").grantedByPlan()).isTrue();
        assertThat(findFeature(resolution, "PLANNED_DEP").effectiveEnabled()).isFalse();
        assertThat(findFeature(resolution, "PLANNED_DEP").warnings())
                .contains("Feature is still PLANNED and cannot be effectively enabled.");
    }

    @Test
    @DisplayName("feature key blank e resolve integer limit para boolean falham com bad request")
    void featureKeyBlankEResolveIntegerLimitParaBooleanFalhamComBadRequest() {
        Tenant tenant = tenant();
        PlatformFeature tracking = feature("TRACKING", PlatformFeatureValueType.BOOLEAN, true, PlatformFeatureImplementationStatus.AVAILABLE);
        SubscriptionPlan plan = plan(UUID.randomUUID(), "STARTER", SubscriptionPlanStatus.ACTIVE);
        setField(plan, "entitlements", Set.of(entitlement(plan, tracking, true, null)));
        TenantSubscription subscription = subscription(tenant, plan, TenantSubscriptionStatus.ACTIVE);

        stubActiveResolution(tenant, List.of(tracking), List.of(), subscription, plan);

        assertThatThrownBy(() -> service.resolveFeatureEntitlement(tenant.getId(), "   "))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("featureKey");
        assertThatThrownBy(() -> service.resolveIntegerLimit(tenant.getId(), "TRACKING"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("INTEGER_LIMIT");
        assertThatThrownBy(() -> service.resolveFeatureEntitlement(tenant.getId(), "UNKNOWN"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Platform feature");
    }

    private void stubActiveResolution(Tenant tenant,
                                      List<PlatformFeature> features,
                                      List<PlatformFeatureDependency> dependencies,
                                      TenantSubscription subscription,
                                      SubscriptionPlan plan) {
        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
        when(platformFeatureRepository.findAll()).thenReturn(features);
        when(platformFeatureDependencyRepository.findAllWithRequiredFeature()).thenReturn(dependencies);
        when(tenantSubscriptionRepository.findAllByTenantIdAndEndedAtIsNullAndStatusInOrderByStartedAtDescCreatedAtDesc(
                tenant.getId(), List.of(TenantSubscriptionStatus.ACTIVE, TenantSubscriptionStatus.SUSPENDED)
        )).thenReturn(List.of(subscription));
        when(subscriptionPlanRepository.findDetailedById(plan.getId())).thenReturn(Optional.of(plan));
    }

    private static void assertFeature(TenantEntitlementResolution resolution,
                                      String featureKey,
                                      boolean grantedByPlan,
                                      boolean effectiveEnabled) {
        assertThat(findFeature(resolution, featureKey).grantedByPlan()).isEqualTo(grantedByPlan);
        assertThat(findFeature(resolution, featureKey).effectiveEnabled()).isEqualTo(effectiveEnabled);
    }

    private static ResolvedFeatureEntitlement findFeature(TenantEntitlementResolution resolution, String featureKey) {
        return resolution.features().stream()
                .filter(item -> item.featureKey().equals(featureKey))
                .findFirst()
                .orElseThrow();
    }

    private static Tenant tenant() {
        Tenant tenant = new Tenant("Tenant One", "tenant-one", "ops@tenant.com", "LEGACY");
        setField(tenant, "id", UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
        return tenant;
    }

    private static PlatformFeature feature(String key,
                                           PlatformFeatureValueType valueType,
                                           boolean active,
                                           PlatformFeatureImplementationStatus implementationStatus) {
        PlatformFeature feature = instantiate(PlatformFeature.class);
        setField(feature, "key", key);
        setField(feature, "name", key + " name");
        setField(feature, "description", key + " description");
        setField(feature, "valueType", valueType);
        setField(feature, "implementationStatus", implementationStatus);
        setField(feature, "active", active);
        setField(feature, "createdAt", Instant.parse("2026-08-01T00:00:00Z"));
        setField(feature, "updatedAt", Instant.parse("2026-08-01T00:00:00Z"));
        return feature;
    }

    private static PlatformFeatureDependency dependency(PlatformFeature feature, PlatformFeature requiredFeature) {
        PlatformFeatureDependency dependency = instantiate(PlatformFeatureDependency.class);
        setField(dependency, "id", new PlatformFeatureDependencyId(feature.getKey(), requiredFeature.getKey()));
        setField(dependency, "feature", feature);
        setField(dependency, "requiredFeature", requiredFeature);
        return dependency;
    }

    private static SubscriptionPlan plan(UUID id, String code, SubscriptionPlanStatus status) {
        SubscriptionPlan plan = instantiate(SubscriptionPlan.class);
        setField(plan, "id", id);
        setField(plan, "code", code);
        setField(plan, "name", code + " name");
        setField(plan, "description", code + " description");
        setField(plan, "status", status);
        setField(plan, "displayOrder", 0);
        setField(plan, "custom", false);
        setField(plan, "createdAt", Instant.parse("2026-08-01T00:00:00Z"));
        setField(plan, "updatedAt", Instant.parse("2026-08-01T00:00:00Z"));
        return plan;
    }

    private static PlanEntitlement entitlement(SubscriptionPlan plan,
                                               PlatformFeature feature,
                                               boolean enabled,
                                               Integer limitValue) {
        PlanEntitlement entitlement = instantiate(PlanEntitlement.class);
        setField(entitlement, "id", new PlanEntitlementId(plan.getId(), feature.getKey()));
        setField(entitlement, "plan", plan);
        setField(entitlement, "feature", feature);
        setField(entitlement, "enabled", enabled);
        setField(entitlement, "limitValue", limitValue);
        return entitlement;
    }

    private static TenantSubscription subscription(Tenant tenant,
                                                   SubscriptionPlan plan,
                                                   TenantSubscriptionStatus status) {
        TenantSubscription subscription = new TenantSubscription(tenant, plan, status, Instant.parse("2026-08-01T12:00:00Z"), "reason", null);
        setField(subscription, "id", UUID.randomUUID());
        return subscription;
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static <T> T instantiate(Class<T> type) {
        try {
            var constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }
}
