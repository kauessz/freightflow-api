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
    Instant lastLoginAt,
    Instant createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId().toString(),
            user.getName(),
            user.getEmail(),
            user.getRole().name(),
            user.getTenant().getId().toString(),
            user.getTenant().getName(),
            user.getLastLoginAt(),
            user.getCreatedAt()
        );
    }
}
