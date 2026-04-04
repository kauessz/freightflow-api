package com.freightflow.shared.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class UserPrincipal implements UserDetails {

    private final UUID id;
    private final String email;
    private final String password;
    private final UUID tenantId;
    private final String role;
    /** Non-null only for CLIENT role — scopes shipment visibility */
    private final UUID customerId;

    public UserPrincipal(UUID id, String email, String password, UUID tenantId, String role, UUID customerId) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.tenantId = tenantId;
        this.role = role;
        this.customerId = customerId;
    }

    /**
     * Factory method para criar UserPrincipal a partir do token JWT
     * (sem password, usado apenas para autenticacao via token).
     */
    public static UserPrincipal fromToken(UUID id, String email, UUID tenantId, String role, UUID customerId) {
        return new UserPrincipal(id, email, null, tenantId, role, customerId);
    }

    // ==================== UserDetails ====================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() { return password; }

    @Override
    public String getUsername() { return email; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    // ==================== Custom getters ====================

    public UUID getId() { return id; }

    public String getEmail() { return email; }

    public UUID getTenantId() { return tenantId; }

    public String getRole() { return role; }

    public UUID getCustomerId() { return customerId; }
}
