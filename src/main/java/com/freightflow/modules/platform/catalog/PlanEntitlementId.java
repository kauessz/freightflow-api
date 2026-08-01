package com.freightflow.modules.platform.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class PlanEntitlementId implements Serializable {

    @Column(name = "plan_id", nullable = false)
    private UUID planId;

    @Column(name = "feature_key", nullable = false, length = 80)
    private String featureKey;

    protected PlanEntitlementId() {
    }

    public PlanEntitlementId(UUID planId, String featureKey) {
        this.planId = planId;
        this.featureKey = normalize(featureKey);
    }

    @jakarta.persistence.PrePersist
    @jakarta.persistence.PreUpdate
    void normalizeState() {
        this.featureKey = normalize(featureKey);
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    public UUID getPlanId() {
        return planId;
    }

    public String getFeatureKey() {
        return featureKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlanEntitlementId that)) return false;
        return Objects.equals(planId, that.planId) && Objects.equals(featureKey, that.featureKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(planId, featureKey);
    }
}
