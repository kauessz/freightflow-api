package com.freightflow.modules.platform.subscription;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TenantSubscriptionEventRepository extends JpaRepository<TenantSubscriptionEvent, UUID> {

    @EntityGraph(attributePaths = {"previousPlan", "newPlan"})
    List<TenantSubscriptionEvent> findAllByTenantIdOrderByCreatedAtDesc(UUID tenantId);

    @EntityGraph(attributePaths = {"previousPlan", "newPlan"})
    List<TenantSubscriptionEvent> findAllByTenantSubscriptionIdOrderByCreatedAtDesc(UUID tenantSubscriptionId);
}
