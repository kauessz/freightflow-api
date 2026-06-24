package com.freightflow.modules.port;

import jakarta.persistence.*;
import java.util.UUID;
import java.time.Instant;

@Entity
@Table(name = "ports")
public class Port {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 5)
    private String unlocode;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 2)
    private String country;

    @Column(nullable = false)
    private String timezone;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Port() {}

    public Port(String unlocode, String name, String country, String timezone, Double latitude, Double longitude) {
        this.unlocode = unlocode;
        this.name = name;
        this.country = country;
        this.timezone = timezone;
        this.latitude = latitude;
        this.longitude = longitude;
        this.active = true;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getUnlocode() {
        return unlocode;
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public String getTimezone() {
        return timezone;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUnlocode(String unlocode) {
        this.unlocode = unlocode;
        this.updatedAt = Instant.now();
    }

    public void setName(String name) {
        this.name = name;
        this.updatedAt = Instant.now();
    }

    public void setCountry(String country) {
        this.country = country;
        this.updatedAt = Instant.now();
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
        this.updatedAt = Instant.now();
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
        this.updatedAt = Instant.now();
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
        this.updatedAt = Instant.now();
    }

    public void setActive(boolean active) {
        this.active = active;
        this.updatedAt = Instant.now();
    }
}
