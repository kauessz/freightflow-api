package com.freightflow.modules.platform.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "plan_entitlements")
public class PlanEntitlement {

    @EmbeddedId
    private PlanEntitlementId id;

    @MapsId("planId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false, updatable = false)
    private SubscriptionPlan plan;

    @MapsId("featureKey")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feature_key", nullable = false, updatable = false)
    private PlatformFeature feature;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "limit_value")
    private Integer limitValue;

    protected PlanEntitlement() {
    }

    public PlanEntitlement(SubscriptionPlan plan, PlatformFeature feature, boolean enabled, Integer limitValue) {
        this.plan = plan;
        this.feature = feature;
        this.enabled = enabled;
        this.limitValue = limitValue;
        this.id = new PlanEntitlementId(plan.getId(), feature.getKey());
    }

    public PlanEntitlementId getId() {
        return id;
    }

    public SubscriptionPlan getPlan() {
        return plan;
    }

    public PlatformFeature getFeature() {
        return feature;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Integer getLimitValue() {
        return limitValue;
    }
}
