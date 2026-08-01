package com.freightflow.modules.platform.dto;

public record PlatformAuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        PlatformUserInfo user
) {
    public PlatformAuthResponse(String accessToken, long expiresIn, PlatformUserInfo user) {
        this(accessToken, "Bearer", expiresIn, user);
    }

    public record PlatformUserInfo(
            String id,
            String email,
            String platformRole,
            String status,
            String lastLoginAt
    ) {
    }
}

