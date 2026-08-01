package com.freightflow.modules.platform.catalog;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "platform_feature_dependencies")
public class PlatformFeatureDependency {

    @EmbeddedId
    private PlatformFeatureDependencyId id;

    @MapsId("featureKey")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feature_key", nullable = false, updatable = false)
    private PlatformFeature feature;

    @MapsId("requiredFeatureKey")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "required_feature_key", nullable = false, updatable = false)
    private PlatformFeature requiredFeature;

    protected PlatformFeatureDependency() {
    }

    public PlatformFeatureDependency(PlatformFeature feature, PlatformFeature requiredFeature) {
        this.feature = feature;
        this.requiredFeature = requiredFeature;
        this.id = new PlatformFeatureDependencyId(feature.getKey(), requiredFeature.getKey());
    }

    public PlatformFeatureDependencyId getId() {
        return id;
    }

    public PlatformFeature getFeature() {
        return feature;
    }

    public PlatformFeature getRequiredFeature() {
        return requiredFeature;
    }
}
