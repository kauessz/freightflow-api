package com.freightflow.modules.voyage;

import jakarta.persistence.*;
import com.freightflow.modules.vessel.Vessel;
import com.freightflow.modules.port.Port;
import com.freightflow.modules.shipment.Shipment;
import com.freightflow.modules.voyage.enums.VoyageStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.time.Instant;
import java.time.Duration;

@Entity
@Table(name = "voyages")
public class Voyage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String voyageNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vessel_id", nullable = false)
    private Vessel vessel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_port_id", nullable = false)
    private Port originPort;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_port_id", nullable = false)
    private Port destinationPort;

    @Column(nullable = false)
    private Instant etd;

    @Column(nullable = false)
    private Instant eta;

    @Column
    private Instant atd;

    @Column
    private Instant ata;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private VoyageStatus status;

    @Column(nullable = false)
    private boolean active;

    @OneToMany(mappedBy = "voyage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Shipment> shipments = new ArrayList<>();

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Voyage() {}

    public Voyage(String voyageNumber, Vessel vessel, Port originPort, Port destinationPort,
                  Instant etd, Instant eta) {
        this.voyageNumber = voyageNumber;
        this.vessel = vessel;
        this.originPort = originPort;
        this.destinationPort = destinationPort;
        this.etd = etd;
        this.eta = eta;
        this.status = VoyageStatus.SCHEDULED;
        this.active = true;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public long estimatedTransitTimeHours() {
        return Duration.between(etd, eta).toHours();
    }

    public boolean isDelayed() {
        if (atd == null) return etd.isBefore(Instant.now());
        if (ata == null) return eta.isBefore(Instant.now());
        return ata.isAfter(eta);
    }

    public long delayHours() {
        if (ata == null) return 0;
        return Duration.between(eta, ata).toHours();
    }

    public UUID getId() {
        return id;
    }

    public String getVoyageNumber() {
        return voyageNumber;
    }

    public void setVoyageNumber(String voyageNumber) {
        this.voyageNumber = voyageNumber;
        this.updatedAt = Instant.now();
    }

    public Vessel getVessel() {
        return vessel;
    }

    public void setVessel(Vessel vessel) {
        this.vessel = vessel;
        this.updatedAt = Instant.now();
    }

    public Port getOriginPort() {
        return originPort;
    }

    public void setOriginPort(Port originPort) {
        this.originPort = originPort;
        this.updatedAt = Instant.now();
    }

    public Port getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationPort(Port destinationPort) {
        this.destinationPort = destinationPort;
        this.updatedAt = Instant.now();
    }

    public Instant getEtd() {
        return etd;
    }

    public void setEtd(Instant etd) {
        this.etd = etd;
        this.updatedAt = Instant.now();
    }

    public Instant getEta() {
        return eta;
    }

    public void setEta(Instant eta) {
        this.eta = eta;
        this.updatedAt = Instant.now();
    }

    public Instant getAtd() {
        return atd;
    }

    public void setAtd(Instant atd) {
        this.atd = atd;
        this.updatedAt = Instant.now();
    }

    public Instant getAta() {
        return ata;
    }

    public void setAta(Instant ata) {
        this.ata = ata;
        this.updatedAt = Instant.now();
    }

    public VoyageStatus getStatus() {
        return status;
    }

    public void setStatus(VoyageStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public List<Shipment> getShipments() {
        return shipments;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        this.updatedAt = Instant.now();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
