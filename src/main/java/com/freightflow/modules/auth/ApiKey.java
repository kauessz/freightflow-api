package com.freightflow.modules.auth;

import jakarta.persistence.*;
import java.util.UUID;
import java.time.Instant;

@Entity
@Table(name = "api_keys")
public class ApiKey {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String keyHash;

    @Column(nullable = false, length = 8)
    private String keyPrefix;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    @Column(nullable = false)
    private boolean active;

    @Column
    private Instant expiresAt;

    @Column
    private Instant lastUsedAt;

    @Column(nullable = false)
    private Instant createdAt;

    protected ApiKey() {}

    public ApiKey(String keyHash, String keyPrefix, String name, Tenant tenant, User createdBy) {
        this.keyHash = keyHash;
        this.keyPrefix = keyPrefix;
        this.name = name;
        this.tenant = tenant;
        this.createdBy = createdBy;
        this.active = true;
        this.createdAt = Instant.now();
    }

    public boolean isExpired() {
        if (expiresAt == null) return false;
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return active && !isExpired();
    }

    public void revoke() {
        this.active = false;
    }

    public void recordUsage() {
        this.lastUsedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getKeyHash() {
        return keyHash;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public String getName() {
        return name;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getLastUsedAt() {
        return lastUsedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
