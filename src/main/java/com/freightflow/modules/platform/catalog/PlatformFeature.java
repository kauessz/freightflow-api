package com.freightflow.modules.platform.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

@Entity
@Table(name = "platform_features")
public class PlatformFeature {

    @Id
    @Column(name = "feature_key", nullable = false, updatable = false, length = 80)
    private String key;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "value_type", nullable = false, length = 30)
    private PlatformFeatureValueType valueType;

    @Column(length = 50)
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(name = "implementation_status", nullable = false, length = 30)
    private PlatformFeatureImplementationStatus implementationStatus;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "feature")
    private Set<PlatformFeatureDependency> requiredFeatures = new LinkedHashSet<>();

    protected PlatformFeature() {
    }

    @PrePersist
    @PreUpdate
    void normalizeState() {
        this.key = normalizeKey(key);
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        updatedAt = Instant.now();
    }

    private String normalizeKey(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public PlatformFeatureValueType getValueType() {
        return valueType;
    }

    public String getUnit() {
        return unit;
    }

    public PlatformFeatureImplementationStatus getImplementationStatus() {
        return implementationStatus;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Set<PlatformFeatureDependency> getRequiredFeatures() {
        return requiredFeatures;
    }
}
