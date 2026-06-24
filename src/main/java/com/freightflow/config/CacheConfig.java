package com.freightflow.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

/**
 * Cache configuration.
 *
 * Non-test profiles: RedisCacheManager with per-cache TTLs.
 *   - "ais-positions"       : configurable via freightflow.cache.ais-ttl-minutes (default 5)
 *   - "vessel-data"         : fixed 30 minutes
 *   - "analytics-dashboard" : fixed 2 minutes (ops dashboard, delays, performance)
 *
 * Test profile: ConcurrentMapCacheManager — no Redis dependency needed for unit/integration tests.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /** Configurable AIS position TTL; override per-profile in application.yml */
    @Value("${freightflow.cache.ais-ttl-minutes:5}")
    private long aisTtlMinutes;

    // ── Redis cache (dev / railway / prod) ──────────────────────────────────

    @Bean
    @Profile("!test")
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper cacheMapper = buildCacheObjectMapper();
        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(cacheMapper);

        RedisCacheConfiguration base = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(SerializationPair.fromSerializer(jsonSerializer))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(base)
                .withInitialCacheConfigurations(Map.of(
                        "ais-positions",       base.entryTtl(Duration.ofMinutes(aisTtlMinutes)),
                        "vessel-data",         base.entryTtl(Duration.ofMinutes(30)),
                        "analytics-dashboard", base.entryTtl(Duration.ofMinutes(2))
                ))
                .build();
    }

    /**
     * ObjectMapper dedicated to Redis serialization.
     *
     * Uses DefaultTyping.EVERYTHING so that @class is embedded for all types —
     * including Java records (which are implicitly final) such as AisPositionResponse.
     * This lets GenericJackson2JsonRedisSerializer reconstruct the correct type on
     * cache hits without requiring explicit type hints at the call site.
     */
    private ObjectMapper buildCacheObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY
        );
        return mapper;
    }

    // ── In-process fallback (test profile only) ─────────────────────────────

    @Bean
    @Profile("test")
    public CacheManager concurrentCacheManager() {
        // Unit tests mock AisClient directly, so no Redis infrastructure is needed.
        // Integration tests that need cache behaviour should spin up Redis via Testcontainers.
        return new ConcurrentMapCacheManager("ais-positions", "vessel-data", "analytics-dashboard");
    }
}
