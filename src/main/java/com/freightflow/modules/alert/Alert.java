package com.freightflow.modules.alert;

import jakarta.persistence.*;
import com.freightflow.modules.shipment.Shipment;
import com.freightflow.modules.alert.enums.AlertType;
import com.freightflow.modules.alert.enums.Severity;
import java.util.UUID;
import java.time.Instant;

@Entity
@Table(name = "alerts")
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AlertType type;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Severity severity;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private boolean resolved;

    @Column
    private Instant resolvedAt;

    @Column(nullable = false)
    private Instant createdAt;

    protected Alert() {}

    public Alert(Shipment shipment, AlertType type, Severity severity, String message) {
        this.shipment = shipment;
        this.type = type;
        this.severity = severity;
        this.message = message;
        this.resolved = false;
        this.createdAt = Instant.now();
    }

    public void resolve() {
        this.resolved = true;
        this.resolvedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Shipment getShipment() {
        return shipment;
    }

    public AlertType getType() {
        return type;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getMessage() {
        return message;
    }

    public boolean isResolved() {
        return resolved;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
