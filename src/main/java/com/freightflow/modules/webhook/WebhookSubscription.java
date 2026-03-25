package com.freightflow.modules.webhook;

import jakarta.persistence.*;
import com.freightflow.modules.auth.Tenant;
import java.util.UUID;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "webhook_subscriptions")
public class WebhookSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String secret;

    @Column(nullable = false)
    private String events;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false)
    private boolean active;

    @Column
    private Instant lastTriggeredAt;

    @Column(nullable = false)
    private int failureCount;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected WebhookSubscription() {}

    public WebhookSubscription(String url, String secret, String events, Tenant tenant) {
        this.url = url;
        this.secret = secret;
        this.events = events;
        this.tenant = tenant;
        this.active = true;
        this.failureCount = 0;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public boolean listensTo(String event) {
        Set<String> subscribedEvents = new HashSet<>(Arrays.asList(events.split(",")));
        return subscribedEvents.contains(event.trim()) || subscribedEvents.contains("*");
    }

    public void recordSuccess() {
        this.failureCount = 0;
        this.lastTriggeredAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void recordFailure() {
        this.failureCount++;
        if (this.failureCount >= 10) {
            this.active = false;
        }
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getSecret() {
        return secret;
    }

    public String getEvents() {
        return events;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        this.updatedAt = Instant.now();
    }

    public Instant getLastTriggeredAt() {
        return lastTriggeredAt;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
