package com.freightflow.modules.shipment;

import jakarta.persistence.*;
import com.freightflow.modules.voyage.Voyage;
import com.freightflow.modules.port.Port;
import com.freightflow.modules.auth.Tenant;
import com.freightflow.modules.shipment.enums.ShipmentStatus;
import com.freightflow.modules.shipment.enums.ContainerType;
import com.freightflow.modules.event.Event;
import com.freightflow.modules.event.enums.EventType;
import com.freightflow.modules.alert.Alert;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.time.Instant;

@Entity
@Table(name = "shipments")
public class Shipment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String booking;

    @Column(length = 11)
    private String containerNumber;

    @Column
    @Enumerated(EnumType.STRING)
    private ContainerType containerType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voyage_id", nullable = false)
    private Voyage voyage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_port_id", nullable = false)
    private Port originPort;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_port_id", nullable = false)
    private Port destinationPort;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column
    private String consignee;

    @Column
    private String shipper;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ShipmentStatus status;

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("occurredAt DESC")
    private List<Event> events = new ArrayList<>();

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Alert> alerts = new ArrayList<>();

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Shipment() {}

    public Shipment(String booking, Voyage voyage, Port originPort, Port destinationPort, Tenant tenant) {
        this.booking = booking;
        this.voyage = voyage;
        this.originPort = originPort;
        this.destinationPort = destinationPort;
        this.tenant = tenant;
        this.status = ShipmentStatus.BOOKED;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void addEvent(Event event) {
        this.events.add(event);
        updateStatusFromEvent(event);
        this.updatedAt = Instant.now();
    }

    private void updateStatusFromEvent(Event event) {
        switch (event.getType()) {
            case GATE_IN -> this.status = ShipmentStatus.GATE_IN;
            case LOADED -> this.status = ShipmentStatus.LOADED;
            case DEPARTED -> this.status = ShipmentStatus.IN_TRANSIT;
            case ARRIVED -> this.status = ShipmentStatus.ARRIVED;
            case GATE_OUT -> this.status = ShipmentStatus.GATE_OUT;
            case CUSTOMS_RELEASE -> {
                if (this.status == ShipmentStatus.ARRIVED) {
                    this.status = ShipmentStatus.DELIVERED;
                }
            }
            default -> {}
        }
    }

    public Event lastEvent() {
        return events.isEmpty() ? null : events.get(0);
    }

    public boolean hasUnresolvedAlerts() {
        return alerts.stream().anyMatch(alert -> !alert.isResolved());
    }

    public UUID getId() {
        return id;
    }

    public String getBooking() {
        return booking;
    }

    public String getContainerNumber() {
        return containerNumber;
    }

    public void setContainerNumber(String containerNumber) {
        this.containerNumber = containerNumber;
        this.updatedAt = Instant.now();
    }

    public ContainerType getContainerType() {
        return containerType;
    }

    public void setContainerType(ContainerType containerType) {
        this.containerType = containerType;
        this.updatedAt = Instant.now();
    }

    public Voyage getVoyage() {
        return voyage;
    }

    public Port getOriginPort() {
        return originPort;
    }

    public Port getDestinationPort() {
        return destinationPort;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public String getConsignee() {
        return consignee;
    }

    public void setConsignee(String consignee) {
        this.consignee = consignee;
        this.updatedAt = Instant.now();
    }

    public String getShipper() {
        return shipper;
    }

    public void setShipper(String shipper) {
        this.shipper = shipper;
        this.updatedAt = Instant.now();
    }

    public ShipmentStatus getStatus() {
        return status;
    }

    public void setStatus(ShipmentStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public List<Event> getEvents() {
        return events;
    }

    public List<Alert> getAlerts() {
        return alerts;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
