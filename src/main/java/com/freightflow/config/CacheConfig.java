package com.freightflow.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Cache configuration.
 * In dev/test profiles (without Redis), uses ConcurrentMapCacheManager.
 * In production with Redis, Spring Boot auto-configures RedisCacheManager
 * and this bean is overridden.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    @Profile({"dev", "test", "default"})
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("ais-positions");
    }
}
