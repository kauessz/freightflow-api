package com.freightflow.modules.customer;

import com.freightflow.modules.auth.Tenant;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false)
    private String name;

    @Column(name = "tax_id", length = 50)
    private String taxId;

    @Column(name = "contact_name")
    private String contactName;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Customer() {}

    public Customer(Tenant tenant, String name) {
        this.tenant = tenant;
        this.name = name;
        this.active = true;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // ==================== Getters / Setters ====================

    public UUID getId() { return id; }

    public Tenant getTenant() { return tenant; }

    public String getName() { return name; }

    public void setName(String name) {
        this.name = name;
        this.updatedAt = Instant.now();
    }

    public String getTaxId() { return taxId; }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
        this.updatedAt = Instant.now();
    }

    public String getContactName() { return contactName; }

    public void setContactName(String contactName) {
        this.contactName = contactName;
        this.updatedAt = Instant.now();
    }

    public String getContactEmail() { return contactEmail; }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
        this.updatedAt = Instant.now();
    }

    public boolean isActive() { return active; }

    public void setActive(boolean active) {
        this.active = active;
        this.updatedAt = Instant.now();
    }

    public Instant getCreatedAt() { return createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
}
