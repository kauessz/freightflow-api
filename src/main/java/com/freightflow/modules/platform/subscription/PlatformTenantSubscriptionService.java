package com.freightflow.modules.platform.subscription;

import com.freightflow.modules.auth.Tenant;
import com.freightflow.modules.auth.TenantRepository;
import com.freightflow.modules.platform.catalog.SubscriptionPlan;
import com.freightflow.modules.platform.catalog.SubscriptionPlanRepository;
import com.freightflow.modules.platform.catalog.SubscriptionPlanStatus;
import com.freightflow.modules.platform.subscription.dto.SubscriptionPlanSnapshotResponse;
import com.freightflow.modules.platform.subscription.dto.TenantSubscriptionCurrentResponse;
import com.freightflow.modules.platform.subscription.dto.TenantSubscriptionEventResponse;
import com.freightflow.modules.platform.subscription.dto.TenantSubscriptionHistoryResponse;
import com.freightflow.modules.platform.subscription.dto.TenantSubscriptionResponse;
import com.freightflow.shared.exception.BadRequestException;
import com.freightflow.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PlatformTenantSubscriptionService {

    private final TenantRepository tenantRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final TenantSubscriptionRepository tenantSubscriptionRepository;
    private final TenantSubscriptionEventRepository tenantSubscriptionEventRepository;

    public PlatformTenantSubscriptionService(TenantRepository tenantRepository,
                                             SubscriptionPlanRepository subscriptionPlanRepository,
                                             TenantSubscriptionRepository tenantSubscriptionRepository,
                                             TenantSubscriptionEventRepository tenantSubscriptionEventRepository) {
        this.tenantRepository = tenantRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.tenantSubscriptionRepository = tenantSubscriptionRepository;
        this.tenantSubscriptionEventRepository = tenantSubscriptionEventRepository;
    }

    public TenantSubscriptionCurrentResponse getCurrentSubscription(UUID tenantId) {
        Tenant tenant = requireTenant(tenantId);
        TenantSubscriptionResponse currentSubscription = tenantSubscriptionRepository.findOpenByTenantId(tenantId)
                .map(this::toSubscriptionResponse)
                .orElse(null);
        return new TenantSubscriptionCurrentResponse(tenant.getId(), currentSubscription);
    }

    public TenantSubscriptionHistoryResponse getHistory(UUID tenantId) {
        Tenant tenant = requireTenant(tenantId);
        List<TenantSubscriptionResponse> subscriptions = tenantSubscriptionRepository
                .findAllByTenantIdOrderByStartedAtDescCreatedAtDesc(tenantId)
                .stream()
                .map(this::toSubscriptionResponse)
                .toList();
        List<TenantSubscriptionEventResponse> events = tenantSubscriptionEventRepository
                .findAllByTenantIdOrderByCreatedAtDesc(tenantId)
                .stream()
                .map(this::toEventResponse)
                .toList();
        return new TenantSubscriptionHistoryResponse(tenant.getId(), subscriptions, events);
    }

    @Transactional
    public TenantSubscriptionResponse assignPlan(UUID tenantId, String rawPlanCode, String rawReason) {
        Tenant tenant = requireTenant(tenantId);
        if (tenantSubscriptionRepository.findOpenByTenantId(tenantId).isPresent()) {
            throw new BadRequestException("Tenant already has an open subscription. Use change-plan, reactivate or cancel first.");
        }

        SubscriptionPlan plan = requireAssignablePlan(rawPlanCode);
        String reason = normalizeOptionalReason(rawReason);
        Instant now = Instant.now();

        TenantSubscription subscription = tenantSubscriptionRepository.save(new TenantSubscription(
                tenant,
                plan,
                TenantSubscriptionStatus.ACTIVE,
                now,
                reason,
                null
        ));
        tenantSubscriptionEventRepository.save(new TenantSubscriptionEvent(
                subscription,
                tenant,
                TenantSubscriptionEventType.SUBSCRIPTION_ASSIGNED,
                null,
                plan,
                null,
                TenantSubscriptionStatus.ACTIVE,
                reason,
                now
        ));
        return toSubscriptionResponse(subscription);
    }

    @Transactional
    public TenantSubscriptionResponse changePlan(UUID tenantId, String rawPlanCode, String rawReason) {
        Tenant tenant = requireTenant(tenantId);
        TenantSubscription current = tenantSubscriptionRepository.findActiveByTenantId(tenantId)
                .orElseThrow(() -> new BadRequestException("Tenant does not have an active subscription to change."));
        SubscriptionPlan newPlan = requireAssignablePlan(rawPlanCode);
        if (current.getPlan().getCode().equalsIgnoreCase(newPlan.getCode())) {
            throw new BadRequestException("Tenant already uses this plan.");
        }

        String reason = normalizeOptionalReason(rawReason);
        Instant now = Instant.now();
        SubscriptionPlan previousPlan = current.getPlan();
        current.cancel(now, reason);

        TenantSubscription newSubscription = tenantSubscriptionRepository.save(new TenantSubscription(
                tenant,
                newPlan,
                TenantSubscriptionStatus.ACTIVE,
                now,
                reason,
                null
        ));
        tenantSubscriptionEventRepository.save(new TenantSubscriptionEvent(
                newSubscription,
                tenant,
                TenantSubscriptionEventType.PLAN_CHANGED,
                previousPlan,
                newPlan,
                TenantSubscriptionStatus.ACTIVE,
                TenantSubscriptionStatus.ACTIVE,
                reason,
                now
        ));
        return toSubscriptionResponse(newSubscription);
    }

    @Transactional
    public TenantSubscriptionResponse suspend(UUID tenantId, String rawReason) {
        requireTenant(tenantId);
        TenantSubscription subscription = tenantSubscriptionRepository.findActiveByTenantId(tenantId)
                .orElseThrow(() -> new BadRequestException("Tenant does not have an active subscription to suspend."));
        String reason = normalizeOptionalReason(rawReason);
        Instant now = Instant.now();
        subscription.suspend(now, reason);
        tenantSubscriptionEventRepository.save(new TenantSubscriptionEvent(
                subscription,
                subscription.getTenant(),
                TenantSubscriptionEventType.SUBSCRIPTION_SUSPENDED,
                subscription.getPlan(),
                subscription.getPlan(),
                TenantSubscriptionStatus.ACTIVE,
                TenantSubscriptionStatus.SUSPENDED,
                reason,
                now
        ));
        return toSubscriptionResponse(subscription);
    }

    @Transactional
    public TenantSubscriptionResponse reactivate(UUID tenantId, String rawReason) {
        requireTenant(tenantId);
        TenantSubscription subscription = tenantSubscriptionRepository.findOpenSuspendedByTenantId(tenantId)
                .orElseThrow(() -> new BadRequestException("Tenant does not have a suspended open subscription to reactivate."));
        if (tenantSubscriptionRepository.existsByTenantIdAndEndedAtIsNullAndStatus(tenantId, TenantSubscriptionStatus.ACTIVE)) {
            throw new BadRequestException("Tenant already has an active subscription.");
        }
        String reason = normalizeOptionalReason(rawReason);
        Instant now = Instant.now();
        subscription.reactivate(now, reason);
        tenantSubscriptionEventRepository.save(new TenantSubscriptionEvent(
                subscription,
                subscription.getTenant(),
                TenantSubscriptionEventType.SUBSCRIPTION_REACTIVATED,
                subscription.getPlan(),
                subscription.getPlan(),
                TenantSubscriptionStatus.SUSPENDED,
                TenantSubscriptionStatus.ACTIVE,
                reason,
                now
        ));
        return toSubscriptionResponse(subscription);
    }

    @Transactional
    public TenantSubscriptionResponse cancel(UUID tenantId, String rawReason) {
        requireTenant(tenantId);
        TenantSubscription subscription = tenantSubscriptionRepository.findOpenByTenantId(tenantId)
                .orElseThrow(() -> new BadRequestException("Tenant does not have an open subscription to cancel."));
        String reason = normalizeOptionalReason(rawReason);
        Instant now = Instant.now();
        TenantSubscriptionStatus previousStatus = subscription.getStatus();
        subscription.cancel(now, reason);
        tenantSubscriptionEventRepository.save(new TenantSubscriptionEvent(
                subscription,
                subscription.getTenant(),
                TenantSubscriptionEventType.SUBSCRIPTION_CANCELLED,
                subscription.getPlan(),
                subscription.getPlan(),
                previousStatus,
                TenantSubscriptionStatus.CANCELLED,
                reason,
                now
        ));
        return toSubscriptionResponse(subscription);
    }

    private Tenant requireTenant(UUID tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId));
    }

    private SubscriptionPlan requireAssignablePlan(String rawPlanCode) {
        String planCode = normalizePlanCode(rawPlanCode);
        SubscriptionPlan plan = subscriptionPlanRepository.findByCodeIgnoreCase(planCode)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription plan", planCode));
        if (plan.getStatus() != SubscriptionPlanStatus.ACTIVE) {
            throw new BadRequestException("Only ACTIVE subscription plans can be assigned to tenants in this phase.");
        }
        return plan;
    }

    private String normalizePlanCode(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new BadRequestException("Parameter 'planCode' must not be blank.");
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeOptionalReason(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new BadRequestException("Parameter 'reason' must not be blank when provided.");
        }
        return normalized;
    }

    private TenantSubscriptionResponse toSubscriptionResponse(TenantSubscription subscription) {
        return new TenantSubscriptionResponse(
                subscription.getId(),
                subscription.getTenant().getId(),
                new SubscriptionPlanSnapshotResponse(
                        subscription.getPlan().getId(),
                        subscription.getPlan().getCode(),
                        subscription.getPlan().getName(),
                        subscription.getPlan().getStatus()
                ),
                subscription.getStatus(),
                subscription.getStartedAt(),
                subscription.getEndedAt(),
                subscription.getCreatedAt(),
                subscription.getUpdatedAt(),
                subscription.getReason()
        );
    }

    private TenantSubscriptionEventResponse toEventResponse(TenantSubscriptionEvent event) {
        return new TenantSubscriptionEventResponse(
                event.getId(),
                event.getEventType(),
                event.getPreviousPlan() != null ? event.getPreviousPlan().getCode() : null,
                event.getNewPlan() != null ? event.getNewPlan().getCode() : null,
                event.getPreviousStatus(),
                event.getNewStatus(),
                event.getReason(),
                event.getCreatedAt()
        );
    }
}
