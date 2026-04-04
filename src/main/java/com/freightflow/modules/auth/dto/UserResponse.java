package com.freightflow.modules.auth.dto;

import com.freightflow.modules.auth.User;

import java.time.Instant;

public record UserResponse(
    String id,
    String name,
    String email,
    String role,
    String tenantId,
    String tenantName,
    String customerId,
    String customerName,
    boolean active,
    Instant lastLoginAt,
    Instant createdAt
) {
    public static UserResponse from(User user) {
        String customerId   = user.getCustomer() != null ? user.getCustomer().getId().toString() : null;
        String customerName = user.getCustomer() != null ? user.getCustomer().getName() : null;
        return new UserResponse(
            user.getId().toString(),
            user.getName(),
            user.getEmail(),
            user.getRole().name(),
            user.getTenant().getId().toString(),
            user.getTenant().getName(),
            customerId,
            customerName,
            user.isActive(),
            user.getLastLoginAt(),
            user.getCreatedAt()
        );
    }
}
