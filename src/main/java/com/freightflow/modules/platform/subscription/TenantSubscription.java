package com.freightflow.modules.platform.subscription;

import com.freightflow.modules.auth.Tenant;
import com.freightflow.modules.platform.catalog.SubscriptionPlan;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tenant_subscriptions")
public class TenantSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TenantSubscriptionStatus status;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(length = 255)
    private String reason;

    @Column(name = "internal_notes", columnDefinition = "TEXT")
    private String internalNotes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected TenantSubscription() {
    }

    public TenantSubscription(Tenant tenant,
                              SubscriptionPlan plan,
                              TenantSubscriptionStatus status,
                              Instant startedAt,
                              String reason,
                              String internalNotes) {
        this.tenant = tenant;
        this.plan = plan;
        this.status = status;
        this.startedAt = startedAt;
        this.reason = normalizeOptionalText(reason);
        this.internalNotes = normalizeOptionalText(internalNotes);
        this.createdAt = startedAt;
        this.updatedAt = startedAt;
    }

    @PrePersist
    @PreUpdate
    void normalizeState() {
        this.reason = normalizeOptionalText(reason);
        this.internalNotes = normalizeOptionalText(internalNotes);
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (startedAt == null) {
            startedAt = createdAt;
        }
        updatedAt = Instant.now();
    }

    private String normalizeOptionalText(String value) {
        return value == null ? null : value.trim();
    }

    public void suspend(Instant now, String reason) {
        this.status = TenantSubscriptionStatus.SUSPENDED;
        this.reason = normalizeOptionalText(reason);
        this.updatedAt = now;
    }

    public void reactivate(Instant now, String reason) {
        this.status = TenantSubscriptionStatus.ACTIVE;
        this.reason = normalizeOptionalText(reason);
        this.updatedAt = now;
    }

    public void cancel(Instant now, String reason) {
        this.status = TenantSubscriptionStatus.CANCELLED;
        this.endedAt = now;
        this.reason = normalizeOptionalText(reason);
        this.updatedAt = now;
    }

    public UUID getId() {
        return id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public SubscriptionPlan getPlan() {
        return plan;
    }

    public TenantSubscriptionStatus getStatus() {
        return status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }

    public String getReason() {
        return reason;
    }

    public String getInternalNotes() {
        return internalNotes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
