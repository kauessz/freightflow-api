package com.freightflow.modules.platform.subscription;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TenantSubscriptionRepository extends JpaRepository<TenantSubscription, UUID> {

    @EntityGraph(attributePaths = {"plan"})
    List<TenantSubscription> findAllByTenantIdAndEndedAtIsNullAndStatusInOrderByStartedAtDescCreatedAtDesc(
            UUID tenantId,
            Collection<TenantSubscriptionStatus> statuses
    );

    @EntityGraph(attributePaths = {"plan"})
    List<TenantSubscription> findAllByTenantIdAndEndedAtIsNullAndStatusOrderByStartedAtDescCreatedAtDesc(
            UUID tenantId,
            TenantSubscriptionStatus status
    );

    @EntityGraph(attributePaths = {"plan"})
    List<TenantSubscription> findAllByTenantIdOrderByStartedAtDescCreatedAtDesc(UUID tenantId);

    boolean existsByTenantIdAndEndedAtIsNullAndStatus(UUID tenantId, TenantSubscriptionStatus status);

    default Optional<TenantSubscription> findOpenByTenantId(UUID tenantId) {
        return findAllByTenantIdAndEndedAtIsNullAndStatusInOrderByStartedAtDescCreatedAtDesc(
                tenantId,
                List.of(TenantSubscriptionStatus.ACTIVE, TenantSubscriptionStatus.SUSPENDED)
        ).stream().findFirst();
    }

    default Optional<TenantSubscription> findActiveByTenantId(UUID tenantId) {
        return findAllByTenantIdAndEndedAtIsNullAndStatusOrderByStartedAtDescCreatedAtDesc(
                tenantId,
                TenantSubscriptionStatus.ACTIVE
        ).stream().findFirst();
    }

    default Optional<TenantSubscription> findOpenSuspendedByTenantId(UUID tenantId) {
        return findAllByTenantIdAndEndedAtIsNullAndStatusOrderByStartedAtDescCreatedAtDesc(
                tenantId,
                TenantSubscriptionStatus.SUSPENDED
        ).stream().findFirst();
    }
}
