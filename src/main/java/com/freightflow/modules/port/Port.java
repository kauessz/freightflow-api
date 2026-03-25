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

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private Instant createdAt;

    protected Port() {}

    public Port(String unlocode, String name, String country, String timezone, Double latitude, Double longitude) {
        this.unlocode = unlocode;
        this.name = name;
        this.country = country;
        this.timezone = timezone;
        this.latitude = latitude;
        this.longitude = longitude;
        this.createdAt = Instant.now();
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

    public Instant getCreatedAt() {
        return createdAt;
    }
}
