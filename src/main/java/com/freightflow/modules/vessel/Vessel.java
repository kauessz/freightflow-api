package com.freightflow.modules.vessel;

import jakarta.persistence.*;
import com.freightflow.modules.voyage.Voyage;
import com.freightflow.modules.vessel.enums.VesselType;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.time.Instant;

@Entity
@Table(name = "vessels")
public class Vessel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, length = 7)
    private String imo;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 2)
    private String flag;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private VesselType type;

    @Column(nullable = false)
    private Integer capacityTeu;

    @Column(length = 100)
    private String carrier;

    @Column(nullable = false)
    private boolean active;

    @OneToMany(mappedBy = "vessel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Voyage> voyages = new ArrayList<>();

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Vessel() {}

    public Vessel(String imo, String name, String flag, VesselType type, Integer capacityTeu) {
        this.imo = imo;
        this.name = name;
        this.flag = flag;
        this.type = type;
        this.capacityTeu = capacityTeu;
        this.active = true;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getImo() {
        return imo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.updatedAt = Instant.now();
    }

    public void setImo(String imo) {
        this.imo = imo;
        this.updatedAt = Instant.now();
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
        this.updatedAt = Instant.now();
    }

    public VesselType getType() {
        return type;
    }

    public void setType(VesselType type) {
        this.type = type;
        this.updatedAt = Instant.now();
    }

    public Integer getCapacityTeu() {
        return capacityTeu;
    }

    public void setCapacityTeu(Integer capacityTeu) {
        this.capacityTeu = capacityTeu;
        this.updatedAt = Instant.now();
    }

    public List<Voyage> getVoyages() {
        return voyages;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
        this.updatedAt = Instant.now();
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
