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
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tenant_subscription_events")
public class TenantSubscriptionEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_subscription_id")
    private TenantSubscription tenantSubscription;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private TenantSubscriptionEventType eventType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_plan_id")
    private SubscriptionPlan previousPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_plan_id")
    private SubscriptionPlan newPlan;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 30)
    private TenantSubscriptionStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", length = 30)
    private TenantSubscriptionStatus newStatus;

    @Column(length = 255)
    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected TenantSubscriptionEvent() {
    }

    public TenantSubscriptionEvent(TenantSubscription tenantSubscription,
                                   Tenant tenant,
                                   TenantSubscriptionEventType eventType,
                                   SubscriptionPlan previousPlan,
                                   SubscriptionPlan newPlan,
                                   TenantSubscriptionStatus previousStatus,
                                   TenantSubscriptionStatus newStatus,
                                   String reason,
                                   Instant createdAt) {
        this.tenantSubscription = tenantSubscription;
        this.tenant = tenant;
        this.eventType = eventType;
        this.previousPlan = previousPlan;
        this.newPlan = newPlan;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.reason = normalizeOptionalText(reason);
        this.createdAt = createdAt;
    }

    @PrePersist
    void normalizeState() {
        this.reason = normalizeOptionalText(reason);
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    private String normalizeOptionalText(String value) {
        return value == null ? null : value.trim();
    }

    public UUID getId() {
        return id;
    }

    public TenantSubscription getTenantSubscription() {
        return tenantSubscription;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public TenantSubscriptionEventType getEventType() {
        return eventType;
    }

    public SubscriptionPlan getPreviousPlan() {
        return previousPlan;
    }

    public SubscriptionPlan getNewPlan() {
        return newPlan;
    }

    public TenantSubscriptionStatus getPreviousStatus() {
        return previousStatus;
    }

    public TenantSubscriptionStatus getNewStatus() {
        return newStatus;
    }

    public String getReason() {
        return reason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
