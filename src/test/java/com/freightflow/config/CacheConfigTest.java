package com.freightflow.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
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
        runnerWithProfiles("dev")
                .run(context -> {
                    assertThat(context).hasSingleBean(CacheManager.class);
                    assertThat(context.getBean(CacheManager.class)).isInstanceOf(ConcurrentMapCacheManager.class);
                    assertThat(context).doesNotHaveBean(RedisCacheManager.class);
                });
    }

    @Test
    @DisplayName("Usa cache in-memory no profile test")
    void usaCacheInMemoryNoProfileTest() {
        runnerWithProfiles("test")
                .run(context -> {
                    assertThat(context).hasSingleBean(CacheManager.class);
                    assertThat(context.getBean(CacheManager.class)).isInstanceOf(ConcurrentMapCacheManager.class);
                    assertThat(context).doesNotHaveBean(RedisCacheManager.class);
                });
    }

    @Test
    @DisplayName("Usa RedisCacheManager fora de dev e test")
    void usaRedisCacheManagerForaDeDevETest() {
        runnerWithProfiles("ci")
                .withPropertyValues("freightflow.cache.ais-ttl-minutes=5")
                .run(context -> {
                    assertThat(context).hasSingleBean(CacheManager.class);
                    assertThat(context.getBean(CacheManager.class)).isInstanceOf(RedisCacheManager.class);
                });
    }

    private ApplicationContextRunner runnerWithProfiles(String... profiles) {
        return contextRunner.withInitializer(setActiveProfiles(profiles));
    }

    private ApplicationContextInitializer<ConfigurableApplicationContext> setActiveProfiles(String... profiles) {
        return context -> context.getEnvironment().setActiveProfiles(profiles);
    }

    @Configuration(proxyBeanMethods = false)
    static class RedisConnectionFactoryTestConfig {

        @Bean
        RedisConnectionFactory redisConnectionFactory() {
            return mock(RedisConnectionFactory.class);
        }
    }
}
