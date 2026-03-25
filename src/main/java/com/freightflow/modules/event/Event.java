package com.freightflow.modules.event;

import jakarta.persistence.*;
import com.freightflow.modules.shipment.Shipment;
import com.freightflow.modules.event.enums.EventType;
import java.util.UUID;
import java.time.Instant;

@Entity
@Table(name = "events", indexes = {
    @Index(name = "idx_event_shipment_occurred", columnList = "shipment_id, occurred_at")
})
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EventType type;

    @Column(nullable = false)
    private String location;

    @Column
    private String description;

    @Column(nullable = false)
    private Instant occurredAt;

    @Column(nullable = false, updatable = false)
    private Instant reportedAt;

    protected Event() {}

    public Event(Shipment shipment, EventType type, String location, Instant occurredAt) {
        this.shipment = shipment;
        this.type = type;
        this.location = location;
        this.occurredAt = occurredAt;
        this.reportedAt = Instant.now();
    }

    public Event(Shipment shipment, EventType type, String location, String description, Instant occurredAt) {
        this(shipment, type, location, occurredAt);
        this.description = description;
    }

    public UUID getId() {
        return id;
    }

    public Shipment getShipment() {
        return shipment;
    }

    public EventType getType() {
        return type;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public Instant getReportedAt() {
        return reportedAt;
    }
}
