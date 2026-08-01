package com.freightflow.modules.platform;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "platform_bootstrap_state")
public class PlatformBootstrapState {

    @Id
    @Column(name = "bootstrap_key", nullable = false, length = 100)
    private String bootstrapKey;

    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;

    @Column(name = "platform_user_id")
    private UUID platformUserId;

    protected PlatformBootstrapState() {
    }

    public PlatformBootstrapState(String bootstrapKey, Instant completedAt, UUID platformUserId) {
        this.bootstrapKey = bootstrapKey;
        this.completedAt = completedAt;
        this.platformUserId = platformUserId;
    }

    public String getBootstrapKey() {
        return bootstrapKey;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public UUID getPlatformUserId() {
        return platformUserId;
    }
}

