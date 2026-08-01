package com.freightflow.modules.platform.catalog;

import com.freightflow.modules.platform.catalog.dto.PlatformFeatureResponse;
import com.freightflow.modules.platform.catalog.dto.SubscriptionPlanDetailResponse;
import com.freightflow.shared.exception.ResourceNotFoundException;
import com.freightflow.shared.pagination.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlatformCatalogService")
class PlatformCatalogServiceTest {

    @Mock private PlatformFeatureRepository platformFeatureRepository;
    @Mock private PlatformFeatureDependencyRepository platformFeatureDependencyRepository;
    @Mock private SubscriptionPlanRepository subscriptionPlanRepository;

    @InjectMocks private PlatformCatalogService platformCatalogService;

    @Test
    @DisplayName("lista features com filtros e dependencias")
    void listaFeaturesComFiltrosEDependencias() {
        PlatformFeature fleetMap = feature(
                "FLEET_MAP", "Fleet Map", PlatformFeatureValueType.BOOLEAN,
                null, PlatformFeatureImplementationStatus.AVAILABLE, true
        );

        when(platformFeatureRepository.findByActiveAndValueType(true, PlatformFeatureValueType.BOOLEAN, PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(fleetMap), PageRequest.of(0, 20), 1));
        when(platformFeatureDependencyRepository.findAllByFeatureKeys(List.of("FLEET_MAP")))
                .thenReturn(List.of(dependency(fleetMap, feature(
                        "TRACKING", "Tracking", PlatformFeatureValueType.BOOLEAN,
                        null, PlatformFeatureImplementationStatus.AVAILABLE, true
                ))));

        PageResponse<PlatformFeatureResponse> response = platformCatalogService.listFeatures(
                true, PlatformFeatureValueType.BOOLEAN, PageRequest.of(0, 20)
        );

        assertThat(response.data()).singleElement().satisfies(item -> {
            assertThat(item.key()).isEqualTo("FLEET_MAP");
            assertThat(item.dependencies()).containsExactly("TRACKING");
        });
    }

    @Test
    @DisplayName("busca feature por key ignorando case")
    void buscaFeaturePorKeyIgnorandoCase() {
        PlatformFeature tracking = feature(
                "TRACKING", "Tracking", PlatformFeatureValueType.BOOLEAN,
                null, PlatformFeatureImplementationStatus.AVAILABLE, true
        );
        when(platformFeatureRepository.findById("TRACKING")).thenReturn(Optional.of(tracking));
        when(platformFeatureDependencyRepository.findAllByFeatureKeys(List.of("TRACKING"))).thenReturn(List.of());

        PlatformFeatureResponse response = platformCatalogService.getFeatureByKey("tracking");

        assertThat(response.key()).isEqualTo("TRACKING");
    }

    @Test
    @DisplayName("retorna erro quando feature nao existe")
    void retornaErroQuandoFeatureNaoExiste() {
        when(platformFeatureRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> platformCatalogService.getFeatureByKey("unknown"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Platform feature");
    }

    @Test
    @DisplayName("lista planos com filtro por status")
    void listaPlanosComFiltroPorStatus() {
        SubscriptionPlan professional = plan(
                UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
                "PROFESSIONAL",
                SubscriptionPlanStatus.ACTIVE,
                1,
                false
        );
        when(subscriptionPlanRepository.findByStatus(SubscriptionPlanStatus.ACTIVE, PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(professional), PageRequest.of(0, 20), 1));

        PageResponse<?> response = platformCatalogService.listPlans(
                SubscriptionPlanStatus.ACTIVE,
                PageRequest.of(0, 20)
        );

        assertThat(response.data()).hasSize(1);
    }

    @Test
    @DisplayName("busca plano por code e monta entitlements com boolean, limite e ilimitado")
    void buscaPlanoPorCodeEMontaEntitlements() {
        PlatformFeature shipment = feature(
                "SHIPMENT_MANAGEMENT", "Shipment Management", PlatformFeatureValueType.BOOLEAN,
                null, PlatformFeatureImplementationStatus.AVAILABLE, true
        );
        PlatformFeature users = feature(
                "MAX_ACTIVE_USERS", "Max Active Users", PlatformFeatureValueType.INTEGER_LIMIT,
                "USERS", PlatformFeatureImplementationStatus.AVAILABLE, true
        );
        PlatformFeature api = feature(
                "MAX_MONTHLY_API_REQUESTS", "Max API Requests", PlatformFeatureValueType.INTEGER_LIMIT,
                "REQUESTS_PER_MONTH", PlatformFeatureImplementationStatus.AVAILABLE, true
        );

        SubscriptionPlan plan = plan(UUID.fromString("11111111-1111-1111-1111-111111111111"), "ENTERPRISE", SubscriptionPlanStatus.ACTIVE, 2, false);
        setField(plan, "entitlements", Set.of(
                entitlement(plan, shipment, true, null),
                entitlement(plan, users, true, 25),
                entitlement(plan, api, true, null)
        ));

        when(platformFeatureDependencyRepository.findAllWithRequiredFeature()).thenReturn(List.of());
        when(subscriptionPlanRepository.findDetailedByCode("ENTERPRISE")).thenReturn(Optional.of(plan));
        when(platformFeatureRepository.findAll()).thenReturn(List.of(api, shipment, users));
        when(platformFeatureDependencyRepository.findAllByFeatureKeys(anyCollection()))
                .thenReturn(List.of());

        SubscriptionPlanDetailResponse response = platformCatalogService.getPlanByCode("enterprise");

        assertThat(response.code()).isEqualTo("ENTERPRISE");
        assertThat(response.entitlements())
                .extracting(item -> item.featureKey() + ":" + item.enabled() + ":" + item.limitValue())
                .containsExactly(
                        "MAX_ACTIVE_USERS:true:25",
                        "MAX_MONTHLY_API_REQUESTS:true:null",
                        "SHIPMENT_MANAGEMENT:true:null"
                );
    }

    @Test
    @DisplayName("plano ausente resulta em not found")
    void planoAusenteResultaEmNotFound() {
        when(platformFeatureDependencyRepository.findAllWithRequiredFeature()).thenReturn(List.of());
        when(subscriptionPlanRepository.findDetailedById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> platformCatalogService.getPlanById(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Subscription plan");
    }

    @Test
    @DisplayName("detecta ciclo direto de dependencia")
    void detectaCicloDiretoDeDependencia() {
        PlatformFeature tracking = feature("TRACKING", "Tracking", PlatformFeatureValueType.BOOLEAN, null, PlatformFeatureImplementationStatus.AVAILABLE, true);
        PlatformFeature fleetMap = feature("FLEET_MAP", "Fleet Map", PlatformFeatureValueType.BOOLEAN, null, PlatformFeatureImplementationStatus.AVAILABLE, true);

        when(platformFeatureDependencyRepository.findAllWithRequiredFeature()).thenReturn(List.of(
                dependency(tracking, fleetMap),
                dependency(fleetMap, tracking)
        ));

        assertThatThrownBy(platformCatalogService::validateCatalogConsistency)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cycle");
    }

    @Test
    @DisplayName("detecta ciclo indireto de dependencia")
    void detectaCicloIndiretoDeDependencia() {
        PlatformFeature a = feature("A", "A", PlatformFeatureValueType.BOOLEAN, null, PlatformFeatureImplementationStatus.AVAILABLE, true);
        PlatformFeature b = feature("B", "B", PlatformFeatureValueType.BOOLEAN, null, PlatformFeatureImplementationStatus.AVAILABLE, true);
        PlatformFeature c = feature("C", "C", PlatformFeatureValueType.BOOLEAN, null, PlatformFeatureImplementationStatus.AVAILABLE, true);

        when(platformFeatureDependencyRepository.findAllWithRequiredFeature()).thenReturn(List.of(
                dependency(a, b),
                dependency(b, c),
                dependency(c, a)
        ));

        assertThatThrownBy(platformCatalogService::validateCatalogConsistency)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cycle");
    }

    @Test
    @DisplayName("rejeita boolean com limit value inconsistente")
    void rejeitaBooleanComLimitValueInconsistente() {
        PlatformFeature shipment = feature(
                "SHIPMENT_MANAGEMENT", "Shipment Management", PlatformFeatureValueType.BOOLEAN,
                null, PlatformFeatureImplementationStatus.AVAILABLE, true
        );
        SubscriptionPlan plan = plan(UUID.fromString("22222222-2222-2222-2222-222222222222"), "STARTER", SubscriptionPlanStatus.ACTIVE, 0, false);
        setField(plan, "entitlements", Set.of(entitlement(plan, shipment, true, 1)));

        when(platformFeatureDependencyRepository.findAllWithRequiredFeature()).thenReturn(List.of());
        when(subscriptionPlanRepository.findDetailedByCode("STARTER")).thenReturn(Optional.of(plan));
        when(platformFeatureRepository.findAll()).thenReturn(List.of(shipment));
        when(platformFeatureDependencyRepository.findAllByFeatureKeys(List.of("SHIPMENT_MANAGEMENT"))).thenReturn(List.of());

        assertThatThrownBy(() -> platformCatalogService.getPlanByCode("starter"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("BOOLEAN feature");
    }

    private static PlatformFeature feature(String key,
                                           String name,
                                           PlatformFeatureValueType valueType,
                                           String unit,
                                           PlatformFeatureImplementationStatus implementationStatus,
                                           boolean active) {
        PlatformFeature feature = new PlatformFeature();
        setField(feature, "key", key);
        setField(feature, "name", name);
        setField(feature, "description", name + " description");
        setField(feature, "valueType", valueType);
        setField(feature, "unit", unit);
        setField(feature, "implementationStatus", implementationStatus);
        setField(feature, "active", active);
        setField(feature, "createdAt", Instant.parse("2026-08-01T00:00:00Z"));
        setField(feature, "updatedAt", Instant.parse("2026-08-01T00:00:00Z"));
        return feature;
    }

    private static PlatformFeatureDependency dependency(PlatformFeature feature, PlatformFeature requiredFeature) {
        PlatformFeatureDependency dependency = new PlatformFeatureDependency();
        setField(dependency, "id", new PlatformFeatureDependencyId(feature.getKey(), requiredFeature.getKey()));
        setField(dependency, "feature", feature);
        setField(dependency, "requiredFeature", requiredFeature);
        return dependency;
    }

    private static SubscriptionPlan plan(UUID id, String code, SubscriptionPlanStatus status, int displayOrder, boolean custom) {
        SubscriptionPlan plan = new SubscriptionPlan();
        setField(plan, "id", id);
        setField(plan, "code", code);
        setField(plan, "name", code + " name");
        setField(plan, "description", code + " description");
        setField(plan, "status", status);
        setField(plan, "displayOrder", displayOrder);
        setField(plan, "custom", custom);
        setField(plan, "createdAt", Instant.parse("2026-08-01T00:00:00Z"));
        setField(plan, "updatedAt", Instant.parse("2026-08-01T00:00:00Z"));
        return plan;
    }

    private static PlanEntitlement entitlement(SubscriptionPlan plan, PlatformFeature feature, boolean enabled, Integer limitValue) {
        PlanEntitlement entitlement = new PlanEntitlement();
        setField(entitlement, "id", new PlanEntitlementId(plan.getId(), feature.getKey()));
        setField(entitlement, "plan", plan);
        setField(entitlement, "feature", feature);
        setField(entitlement, "enabled", enabled);
        setField(entitlement, "limitValue", limitValue);
        return entitlement;
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
}
