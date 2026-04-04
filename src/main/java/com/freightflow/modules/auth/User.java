package com.freightflow.modules.auth;

import com.freightflow.modules.customer.Customer;
import jakarta.persistence.*;
import java.util.UUID;
import java.time.Instant;

@Entity
@Table(name = "users")
public class User {

    /**
     * ADMIN    — acesso total ao tenant
     * OPERATOR — cria/edita embarques e clientes, não gerencia usuários
     * VIEWER   — somente leitura
     * CLIENT   — visualiza apenas seus próprios embarques (filtrado por customer_id)
     */
    public enum UserRole {
        ADMIN, OPERATOR, VIEWER, CLIENT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    /** Preenchido apenas para usuários com role CLIENT. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(nullable = false)
    private boolean active;

    @Column
    private Instant lastLoginAt;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected User() {}

    public User(String name, String email, String passwordHash, UserRole role, Tenant tenant) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.tenant = tenant;
        this.active = true;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }

    public String getName() { return name; }

    public void setName(String name) {
        this.name = name;
        this.updatedAt = Instant.now();
    }

    public String getEmail() { return email; }

    public void setEmail(String email) {
        this.email = email;
        this.updatedAt = Instant.now();
    }

    public String getPasswordHash() { return passwordHash; }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        this.updatedAt = Instant.now();
    }

    public UserRole getRole() { return role; }

    public void setRole(UserRole role) {
        this.role = role;
        this.updatedAt = Instant.now();
    }

    public Tenant getTenant() { return tenant; }

    public Customer getCustomer() { return customer; }

    public void setCustomer(Customer customer) {
        this.customer = customer;
        this.updatedAt = Instant.now();
    }

    public boolean isActive() { return active; }

    public void setActive(boolean active) {
        this.active = active;
        this.updatedAt = Instant.now();
    }

    public Instant getLastLoginAt() { return lastLoginAt; }

    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
        this.updatedAt = Instant.now();
    }

    public Instant getCreatedAt() { return createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
}
