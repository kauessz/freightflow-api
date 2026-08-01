package com.freightflow.modules.platform.dto;

import com.freightflow.modules.platform.PlatformUser;

import java.time.Instant;

public record PlatformMeResponse(
        String id,
        String email,
        String platformRole,
        String status,
        Instant lastLoginAt
) {
    public static PlatformMeResponse from(PlatformUser user) {
        return new PlatformMeResponse(
                user.getId().toString(),
                user.getEmail(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getLastLoginAt()
        );
    }
}

