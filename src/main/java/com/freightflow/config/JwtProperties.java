package com.freightflow.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "freightflow.jwt")
public class JwtProperties {

    private String secret = "dev-secret-key-change-in-production";
    private long expirationMs = 86_400_000; // 24h
    private long refreshExpirationMs = 604_800_000; // 7d

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }

    public long getExpirationMs() { return expirationMs; }
    public void setExpirationMs(long expirationMs) { this.expirationMs = expirationMs; }

    public long getRefreshExpirationMs() { return refreshExpirationMs; }
    public void setRefreshExpirationMs(long refreshExpirationMs) { this.refreshExpirationMs = refreshExpirationMs; }
}
