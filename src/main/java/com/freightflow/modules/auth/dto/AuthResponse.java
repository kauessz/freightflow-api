package com.freightflow.modules.auth.dto;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresIn,
    UserInfo user
) {
    public AuthResponse(String accessToken, String refreshToken, long expiresIn, UserInfo user) {
        this(accessToken, refreshToken, "Bearer", expiresIn, user);
    }

    public record UserInfo(
        String id,
        String name,
        String email,
        String role,
        String tenantId,
        String tenantName
    ) {}
}
