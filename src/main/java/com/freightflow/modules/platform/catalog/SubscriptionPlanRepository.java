package com.freightflow.modules.platform.catalog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID> {

    Page<SubscriptionPlan> findByStatus(SubscriptionPlanStatus status, Pageable pageable);

    Optional<SubscriptionPlan> findByCodeIgnoreCase(String code);

    @EntityGraph(attributePaths = {"entitlements", "entitlements.feature"})
    @Query("select p from SubscriptionPlan p where p.id = :id")
    Optional<SubscriptionPlan> findDetailedById(@Param("id") UUID id);

    @EntityGraph(attributePaths = {"entitlements", "entitlements.feature"})
    @Query("select p from SubscriptionPlan p where lower(p.code) = lower(:code)")
    Optional<SubscriptionPlan> findDetailedByCode(@Param("code") String code);
}
