package com.freightflow.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@DisplayName("CacheConfig")
class CacheConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(CacheConfig.class, RedisConnectionFactoryTestConfig.class);

    @Test
    @DisplayName("Usa cache in-memory no profile dev")
    void usaCacheInMemoryNoProfileDev() {
        contextRunner
                .withPropertyValues("spring.profiles.active=dev")
                .run(context -> {
                    assertThat(context).hasSingleBean(CacheManager.class);
                    assertThat(context.getBean(CacheManager.class)).isInstanceOf(ConcurrentMapCacheManager.class);
                    assertThat(context).doesNotHaveBean(RedisCacheManager.class);
                });
    }

    @Test
    @DisplayName("Usa RedisCacheManager fora de dev e test")
    void usaRedisCacheManagerForaDeDevETest() {
        contextRunner
                .withPropertyValues("freightflow.cache.ais-ttl-minutes=5")
                .run(context -> {
                    assertThat(context).hasSingleBean(CacheManager.class);
                    assertThat(context.getBean(CacheManager.class)).isInstanceOf(RedisCacheManager.class);
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class RedisConnectionFactoryTestConfig {

        @Bean
        RedisConnectionFactory redisConnectionFactory() {
            return mock(RedisConnectionFactory.class);
        }
    }
}
