package com.freightflow.config;

import com.freightflow.fixtures.TestDataFactory;
import com.freightflow.modules.alert.Alert;
import com.freightflow.modules.alert.AlertEventConsumer;
import com.freightflow.modules.alert.AlertEventPublisher;
import com.freightflow.modules.alert.AlertRepository;
import com.freightflow.modules.webhook.WebhookEventPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@DisplayName("Rabbit messaging configuration")
class RabbitMessagingConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestMessagingContext.class, RabbitMQConfig.class);

    @Test
    @DisplayName("should not create Rabbit beans or listener when messaging is disabled")
    void should_notCreateRabbitBeansOrListener_whenMessagingDisabled() {
        contextRunner
                .withPropertyValues("freightflow.messaging.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(RabbitTemplate.class);
                    assertThat(context).doesNotHaveBean(TopicExchange.class);
                    assertThat(context).doesNotHaveBean(Queue.class);
                    assertThat(context).doesNotHaveBean(AlertEventConsumer.class);
                    assertThat(context).hasSingleBean(AlertEventPublisher.class);

                    Alert alert = TestDataFactory.alert(TestDataFactory.shipment());
                    TestDataFactory.setEntityId(alert, UUID.randomUUID());

                    assertThatCode(() -> context.getBean(AlertEventPublisher.class).publishCriticalAlert(alert))
                            .doesNotThrowAnyException();
                });
    }

    @Test
    @DisplayName("should create Rabbit beans and listener when messaging is enabled")
    void should_createRabbitBeansAndListener_whenMessagingEnabled() {
        contextRunner
                .withPropertyValues("freightflow.messaging.enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(RabbitTemplate.class);
                    assertThat(context).hasSingleBean(TopicExchange.class);
                    assertThat(context).hasSingleBean(Queue.class);
                    assertThat(context).hasSingleBean(AlertEventConsumer.class);
                    assertThat(context).hasSingleBean(AlertEventPublisher.class);
                });
    }

    @Configuration(proxyBeanMethods = false)
    @Import({AlertEventConsumer.class, AlertEventPublisher.class})
    static class TestMessagingContext {

        @Bean
        ConnectionFactory connectionFactory() {
            return mock(ConnectionFactory.class);
        }

        @Bean
        AlertRepository alertRepository() {
            return mock(AlertRepository.class);
        }

        @Bean
        WebhookEventPublisher webhookEventPublisher() {
            return mock(WebhookEventPublisher.class);
        }
    }
}
