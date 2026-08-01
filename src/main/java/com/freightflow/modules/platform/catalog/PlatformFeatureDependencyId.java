package com.freightflow.modules.platform.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;

@Embeddable
public class PlatformFeatureDependencyId implements Serializable {

    @Column(name = "feature_key", nullable = false, length = 80)
    private String featureKey;

    @Column(name = "required_feature_key", nullable = false, length = 80)
    private String requiredFeatureKey;

    protected PlatformFeatureDependencyId() {
    }

    public PlatformFeatureDependencyId(String featureKey, String requiredFeatureKey) {
        this.featureKey = normalize(featureKey);
        this.requiredFeatureKey = normalize(requiredFeatureKey);
    }

    @jakarta.persistence.PrePersist
    @jakarta.persistence.PreUpdate
    void normalizeState() {
        this.featureKey = normalize(featureKey);
        this.requiredFeatureKey = normalize(requiredFeatureKey);
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    public String getFeatureKey() {
        return featureKey;
    }

    public String getRequiredFeatureKey() {
        return requiredFeatureKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlatformFeatureDependencyId that)) return false;
        return Objects.equals(featureKey, that.featureKey)
                && Objects.equals(requiredFeatureKey, that.requiredFeatureKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(featureKey, requiredFeatureKey);
    }
}
