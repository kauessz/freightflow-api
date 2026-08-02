package com.freightflow.modules.platform.subscription;

import com.freightflow.modules.auth.Tenant;
import com.freightflow.modules.auth.TenantRepository;
import com.freightflow.modules.platform.catalog.SubscriptionPlan;
import com.freightflow.modules.platform.catalog.SubscriptionPlanRepository;
import com.freightflow.modules.platform.catalog.SubscriptionPlanStatus;
import com.freightflow.modules.platform.subscription.dto.TenantSubscriptionHistoryResponse;
import com.freightflow.shared.exception.BadRequestException;
import com.freightflow.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlatformTenantSubscriptionService")
class PlatformTenantSubscriptionServiceTest {

    @Mock private TenantRepository tenantRepository;
    @Mock private SubscriptionPlanRepository subscriptionPlanRepository;
    @Mock private TenantSubscriptionRepository tenantSubscriptionRepository;
    @Mock private TenantSubscriptionEventRepository tenantSubscriptionEventRepository;

    @InjectMocks private PlatformTenantSubscriptionService service;

    @Test
    @DisplayName("assign plano active para tenant sem assinatura aberta")
    void assignPlanoActiveParaTenantSemAssinaturaAberta() {
        Tenant tenant = tenant();
        SubscriptionPlan professional = plan("PROFESSIONAL", SubscriptionPlanStatus.ACTIVE);

        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
        when(tenantSubscriptionRepository.findOpenByTenantId(tenant.getId())).thenReturn(Optional.empty());
        when(subscriptionPlanRepository.findByCodeIgnoreCase("PROFESSIONAL")).thenReturn(Optional.of(professional));
        when(tenantSubscriptionRepository.save(any(TenantSubscription.class))).thenAnswer(invocation -> {
            TenantSubscription subscription = invocation.getArgument(0);
            setField(subscription, "id", UUID.fromString("11111111-1111-1111-1111-111111111111"));
            return subscription;
        });

        var response = service.assignPlan(tenant.getId(), " professional ", "Initial assignment");

        assertThat(response.plan().code()).isEqualTo("PROFESSIONAL");
        assertThat(response.status()).isEqualTo(TenantSubscriptionStatus.ACTIVE);
        verify(tenantSubscriptionEventRepository).save(any(TenantSubscriptionEvent.class));
    }

    @Test
    @DisplayName("nao assign se ja existe assinatura aberta active")
    void naoAssignSeJaExisteAssinaturaAbertaActive() {
        Tenant tenant = tenant();
        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
        when(tenantSubscriptionRepository.findOpenByTenantId(tenant.getId()))
                .thenReturn(Optional.of(subscription(tenant, plan("STARTER", SubscriptionPlanStatus.ACTIVE), TenantSubscriptionStatus.ACTIVE)));

        assertThatThrownBy(() -> service.assignPlan(tenant.getId(), "PROFESSIONAL", null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("open subscription");
    }

    @Test
    @DisplayName("nao assign se ja existe assinatura aberta suspended")
    void naoAssignSeJaExisteAssinaturaAbertaSuspended() {
        Tenant tenant = tenant();
        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
        when(tenantSubscriptionRepository.findOpenByTenantId(tenant.getId()))
                .thenReturn(Optional.of(subscription(tenant, plan("STARTER", SubscriptionPlanStatus.ACTIVE), TenantSubscriptionStatus.SUSPENDED)));

        assertThatThrownBy(() -> service.assignPlan(tenant.getId(), "PROFESSIONAL", null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("open subscription");
    }

    @Test
    @DisplayName("nao assign plano draft")
    void naoAssignPlanoDraft() {
        Tenant tenant = tenant();
        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
        when(tenantSubscriptionRepository.findOpenByTenantId(tenant.getId())).thenReturn(Optional.empty());
        when(subscriptionPlanRepository.findByCodeIgnoreCase("CUSTOM")).thenReturn(Optional.of(plan("CUSTOM", SubscriptionPlanStatus.DRAFT)));

        assertThatThrownBy(() -> service.assignPlan(tenant.getId(), "CUSTOM", null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Only ACTIVE subscription plans");
    }

    @Test
    @DisplayName("nao assign plano archived")
    void naoAssignPlanoArchived() {
        Tenant tenant = tenant();
        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
        when(tenantSubscriptionRepository.findOpenByTenantId(tenant.getId())).thenReturn(Optional.empty());
        when(subscriptionPlanRepository.findByCodeIgnoreCase("LEGACY")).thenReturn(Optional.of(plan("LEGACY", SubscriptionPlanStatus.ARCHIVED)));

        assertThatThrownBy(() -> service.assignPlan(tenant.getId(), "LEGACY", null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Only ACTIVE subscription plans");
    }

    @Test
    @DisplayName("change plan encerra assinatura anterior e cria nova active")
    void changePlanEncerraAssinaturaAnteriorECriaNovaActive() {
        Tenant tenant = tenant();
        SubscriptionPlan starter = plan("STARTER", SubscriptionPlanStatus.ACTIVE);
        SubscriptionPlan professional = plan("PROFESSIONAL", SubscriptionPlanStatus.ACTIVE);
        TenantSubscription current = subscription(tenant, starter, TenantSubscriptionStatus.ACTIVE);

        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
        when(tenantSubscriptionRepository.findActiveByTenantId(tenant.getId())).thenReturn(Optional.of(current));
        when(subscriptionPlanRepository.findByCodeIgnoreCase("PROFESSIONAL")).thenReturn(Optional.of(professional));
        when(tenantSubscriptionRepository.save(any(TenantSubscription.class))).thenAnswer(invocation -> {
            TenantSubscription subscription = invocation.getArgument(0);
            setField(subscription, "id", UUID.fromString("22222222-2222-2222-2222-222222222222"));
            return subscription;
        });

        var response = service.changePlan(tenant.getId(), "professional", "Upgrade");

        assertThat(current.getStatus()).isEqualTo(TenantSubscriptionStatus.CANCELLED);
        assertThat(current.getEndedAt()).isNotNull();
        assertThat(response.plan().code()).isEqualTo("PROFESSIONAL");
        assertThat(response.status()).isEqualTo(TenantSubscriptionStatus.ACTIVE);
    }

    @Test
    @DisplayName("change plan para mesmo plano falha")
    void changePlanParaMesmoPlanoFalha() {
        Tenant tenant = tenant();
        SubscriptionPlan starter = plan("STARTER", SubscriptionPlanStatus.ACTIVE);
        TenantSubscription current = subscription(tenant, starter, TenantSubscriptionStatus.ACTIVE);

        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
        when(tenantSubscriptionRepository.findActiveByTenantId(tenant.getId())).thenReturn(Optional.of(current));
        when(subscriptionPlanRepository.findByCodeIgnoreCase("STARTER")).thenReturn(Optional.of(starter));

        assertThatThrownBy(() -> service.changePlan(tenant.getId(), "starter", null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already uses this plan");
    }

    @Test
    @DisplayName("suspend active")
    void suspendActive() {
        Tenant tenant = tenant();
        TenantSubscription current = subscription(tenant, plan("STARTER", SubscriptionPlanStatus.ACTIVE), TenantSubscriptionStatus.ACTIVE);
        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
        when(tenantSubscriptionRepository.findActiveByTenantId(tenant.getId())).thenReturn(Optional.of(current));

        var response = service.suspend(tenant.getId(), "Manual review");

        assertThat(response.status()).isEqualTo(TenantSubscriptionStatus.SUSPENDED);
        assertThat(current.getEndedAt()).isNull();
    }

    @Test
    @DisplayName("reactivate suspended")
    void reactivateSuspended() {
        Tenant tenant = tenant();
        TenantSubscription current = subscription(tenant, plan("STARTER", SubscriptionPlanStatus.ACTIVE), TenantSubscriptionStatus.SUSPENDED);
        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
        when(tenantSubscriptionRepository.findOpenSuspendedByTenantId(tenant.getId())).thenReturn(Optional.of(current));
        when(tenantSubscriptionRepository.existsByTenantIdAndEndedAtIsNullAndStatus(tenant.getId(), TenantSubscriptionStatus.ACTIVE)).thenReturn(false);

        var response = service.reactivate(tenant.getId(), "Issue resolved");

        assertThat(response.status()).isEqualTo(TenantSubscriptionStatus.ACTIVE);
        assertThat(current.getEndedAt()).isNull();
    }

    @Test
    @DisplayName("cancel active")
    void cancelActive() {
        Tenant tenant = tenant();
        TenantSubscription current = subscription(tenant, plan("STARTER", SubscriptionPlanStatus.ACTIVE), TenantSubscriptionStatus.ACTIVE);
        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
        when(tenantSubscriptionRepository.findOpenByTenantId(tenant.getId())).thenReturn(Optional.of(current));

        var response = service.cancel(tenant.getId(), "Closed");

        assertThat(response.status()).isEqualTo(TenantSubscriptionStatus.CANCELLED);
        assertThat(current.getEndedAt()).isNotNull();
    }

    @Test
    @DisplayName("cancel suspended")
    void cancelSuspended() {
        Tenant tenant = tenant();
        TenantSubscription current = subscription(tenant, plan("STARTER", SubscriptionPlanStatus.ACTIVE), TenantSubscriptionStatus.SUSPENDED);
        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
        when(tenantSubscriptionRepository.findOpenByTenantId(tenant.getId())).thenReturn(Optional.of(current));

        var response = service.cancel(tenant.getId(), "Closed");

        assertThat(response.status()).isEqualTo(TenantSubscriptionStatus.CANCELLED);
        assertThat(current.getEndedAt()).isNotNull();
    }

    @Test
    @DisplayName("history retorna assinaturas e eventos")
    void historyRetornaAssinaturasEEventos() {
        Tenant tenant = tenant();
        SubscriptionPlan starter = plan("STARTER", SubscriptionPlanStatus.ACTIVE);
        SubscriptionPlan professional = plan("PROFESSIONAL", SubscriptionPlanStatus.ACTIVE);
        TenantSubscription older = subscription(tenant, starter, TenantSubscriptionStatus.CANCELLED);
        setField(older, "endedAt", Instant.parse("2026-08-01T10:00:00Z"));
        TenantSubscription current = subscription(tenant, professional, TenantSubscriptionStatus.ACTIVE);
        TenantSubscriptionEvent event = event(current, tenant, TenantSubscriptionEventType.PLAN_CHANGED, starter, professional,
                TenantSubscriptionStatus.ACTIVE, TenantSubscriptionStatus.ACTIVE);

        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
        when(tenantSubscriptionRepository.findAllByTenantIdOrderByStartedAtDescCreatedAtDesc(tenant.getId()))
                .thenReturn(List.of(current, older));
        when(tenantSubscriptionEventRepository.findAllByTenantIdOrderByCreatedAtDesc(tenant.getId()))
                .thenReturn(List.of(event));

        TenantSubscriptionHistoryResponse response = service.getHistory(tenant.getId());

        assertThat(response.subscriptions()).hasSize(2);
        assertThat(response.events()).singleElement().satisfies(item -> {
            assertThat(item.eventType()).isEqualTo(TenantSubscriptionEventType.PLAN_CHANGED);
            assertThat(item.previousPlanCode()).isEqualTo("STARTER");
            assertThat(item.newPlanCode()).isEqualTo("PROFESSIONAL");
        });
    }

    @Test
    @DisplayName("tenant inexistente retorna not found")
    void tenantInexistenteRetornaNotFound() {
        UUID tenantId = UUID.randomUUID();
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCurrentSubscription(tenantId))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(subscriptionPlanRepository, never()).findByCodeIgnoreCase(any());
    }

    private static Tenant tenant() {
        Tenant tenant = new Tenant("Tenant One", "tenant-one", "admin@tenant.com", "LEGACY");
        setField(tenant, "id", UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
        return tenant;
    }

    private static SubscriptionPlan plan(String code, SubscriptionPlanStatus status) {
        try {
            var constructor = SubscriptionPlan.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            SubscriptionPlan plan = constructor.newInstance();
            setField(plan, "id", UUID.randomUUID());
            setField(plan, "code", code);
            setField(plan, "name", code + " plan");
            setField(plan, "description", code + " description");
            setField(plan, "status", status);
            setField(plan, "displayOrder", 1);
            setField(plan, "custom", false);
            setField(plan, "createdAt", Instant.parse("2026-08-01T00:00:00Z"));
            setField(plan, "updatedAt", Instant.parse("2026-08-01T00:00:00Z"));
            return plan;
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static TenantSubscription subscription(Tenant tenant, SubscriptionPlan plan, TenantSubscriptionStatus status) {
        TenantSubscription subscription = new TenantSubscription(
                tenant,
                plan,
                status,
                Instant.parse("2026-08-01T09:00:00Z"),
                "Initial",
                null
        );
        setField(subscription, "id", UUID.randomUUID());
        setField(subscription, "createdAt", Instant.parse("2026-08-01T09:00:00Z"));
        setField(subscription, "updatedAt", Instant.parse("2026-08-01T09:00:00Z"));
        return subscription;
    }

    private static TenantSubscriptionEvent event(TenantSubscription subscription,
                                                 Tenant tenant,
                                                 TenantSubscriptionEventType eventType,
                                                 SubscriptionPlan previousPlan,
                                                 SubscriptionPlan newPlan,
                                                 TenantSubscriptionStatus previousStatus,
                                                 TenantSubscriptionStatus newStatus) {
        TenantSubscriptionEvent event = new TenantSubscriptionEvent(
                subscription,
                tenant,
                eventType,
                previousPlan,
                newPlan,
                previousStatus,
                newStatus,
                "reason",
                Instant.parse("2026-08-01T11:00:00Z")
        );
        setField(event, "id", UUID.randomUUID());
        return event;
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
