package com.freightflow.modules.platform.entitlement;

import com.freightflow.AbstractIntegrationTest;
import com.freightflow.modules.auth.Tenant;
import com.freightflow.modules.auth.TenantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Platform tenant entitlement integration")
class PlatformTenantEntitlementIntegrationTest extends AbstractIntegrationTest {

    private static final UUID PROFESSIONAL_PLAN_ID = UUID.fromString("3f7a13e2-1cf4-45c0-a5f2-3ff7d9bdb102");
    private static final UUID ENTERPRISE_PLAN_ID = UUID.fromString("b7c31d61-7afb-4718-b9c9-b9c59fc0c103");

    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private TenantRepository tenantRepository;
    @Autowired private TenantEntitlementResolverService tenantEntitlementResolverService;

    @Test
    @DisplayName("tenant sem assinatura retorna no subscription com features do catalogo")
    void tenantSemAssinaturaRetornaNoSubscriptionComFeaturesDoCatalogo() {
        Tenant tenant = tenantRepository.saveAndFlush(new Tenant("Tenant One", "tenant-one", "ops@tenant.com", "LEGACY"));

        TenantEntitlementResolution resolution = tenantEntitlementResolverService.resolveTenantEntitlements(tenant.getId());

        assertThat(resolution.accessStatus()).isEqualTo(TenantEntitlementAccessStatus.NO_SUBSCRIPTION);
        assertThat(resolution.subscription()).isNull();
        assertThat(resolution.features()).hasSize(19);
        assertThat(resolution.features()).allMatch(item -> !item.effectiveEnabled());
    }

    @Test
    @DisplayName("professional ativo resolve fleet map com dependencia tracking")
    void professionalAtivoResolveFleetMapComDependenciaTracking() {
        Tenant tenant = tenantRepository.saveAndFlush(new Tenant("Tenant Two", "tenant-two", "ops2@tenant.com", "LEGACY"));
        insertSubscription(tenant.getId(), PROFESSIONAL_PLAN_ID, "ACTIVE");

        TenantEntitlementResolution resolution = tenantEntitlementResolverService.resolveTenantEntitlements(tenant.getId());

        assertThat(resolution.accessStatus()).isEqualTo(TenantEntitlementAccessStatus.ACTIVE);
        assertThat(resolution.subscription()).isNotNull();
        assertThat(resolution.subscription().planCode()).isEqualTo("PROFESSIONAL");
        assertThat(findFeature(resolution, "TRACKING").effectiveEnabled()).isTrue();
        assertThat(findFeature(resolution, "FLEET_MAP").effectiveEnabled()).isTrue();
        assertThat(findFeature(resolution, "FLEET_MAP").dependencies()).containsExactly("TRACKING");
    }

    @Test
    @DisplayName("enterprise ativo resolve limite ilimitado")
    void enterpriseAtivoResolveLimiteIlimitado() {
        Tenant tenant = tenantRepository.saveAndFlush(new Tenant("Tenant Three", "tenant-three", "ops3@tenant.com", "LEGACY"));
        insertSubscription(tenant.getId(), ENTERPRISE_PLAN_ID, "ACTIVE");

        TenantEntitlementResolution resolution = tenantEntitlementResolverService.resolveTenantEntitlements(tenant.getId());

        assertThat(findFeature(resolution, "MAX_ACTIVE_USERS").effectiveEnabled()).isTrue();
        assertThat(findFeature(resolution, "MAX_ACTIVE_USERS").unlimited()).isTrue();
        assertThat(findFeature(resolution, "MAX_ACTIVE_USERS").limitValue()).isNull();
    }

    @Test
    @DisplayName("professional suspenso preserva grant mas desabilita efetividade")
    void professionalSuspensoPreservaGrantMasDesabilitaEfetividade() {
        Tenant tenant = tenantRepository.saveAndFlush(new Tenant("Tenant Four", "tenant-four", "ops4@tenant.com", "LEGACY"));
        insertSubscription(tenant.getId(), PROFESSIONAL_PLAN_ID, "SUSPENDED");

        TenantEntitlementResolution resolution = tenantEntitlementResolverService.resolveTenantEntitlements(tenant.getId());

        assertThat(resolution.accessStatus()).isEqualTo(TenantEntitlementAccessStatus.SUSPENDED);
        assertThat(findFeature(resolution, "FLEET_MAP").grantedByPlan()).isTrue();
        assertThat(findFeature(resolution, "FLEET_MAP").effectiveEnabled()).isFalse();
        assertThat(findFeature(resolution, "FLEET_MAP").warnings()).contains("Subscription is suspended.");
    }

    private void insertSubscription(UUID tenantId, UUID planId, String status) {
        Timestamp now = Timestamp.from(Instant.parse("2026-08-02T10:00:00Z"));
        jdbcTemplate.update("""
                insert into tenant_subscriptions (id, tenant_id, plan_id, status, started_at, ended_at, reason, internal_notes, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(), tenantId, planId, status, now, null, "seed", null, now, now
        );
    }

    private ResolvedFeatureEntitlement findFeature(TenantEntitlementResolution resolution, String featureKey) {
        return resolution.features().stream()
                .filter(item -> item.featureKey().equals(featureKey))
                .findFirst()
                .orElseThrow();
    }
}
