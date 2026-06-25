package com.freightflow.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ topology and serialization configuration.
 *
 * Topology:
 *   Exchange : freightflow.alerts  (topic, durable)
 *   Queue    : freightflow.alerts.critical  (durable)
 *   Binding  : queue → exchange via routing key "alert.critical"
 *
 * Messages are serialized/deserialized with Jackson2JsonMessageConverter so
 * AlertEvent records travel as JSON — no manual ObjectMapper required in consumers.
 */
@Configuration
@ConditionalOnProperty(prefix = "freightflow.messaging", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RabbitMQConfig {

    // ── Topology constants (referenced by publisher and consumer) ───────────

    /** Topic exchange that receives all FreightFlow alert events. */
    public static final String EXCHANGE = "freightflow.alerts";

    /** Durable queue for HIGH / CRITICAL alerts. */
    public static final String QUEUE = "freightflow.alerts.critical";

    /** Routing key used when publishing critical alert events. */
    public static final String ROUTING_KEY = "alert.critical";

    // ── Exchange / Queue / Binding beans ────────────────────────────────────

    @Bean
    public TopicExchange alertsExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue criticalAlertQueue() {
        return QueueBuilder.durable(QUEUE).build();
    }

    @Bean
    public Binding criticalAlertBinding(Queue criticalAlertQueue, TopicExchange alertsExchange) {
        return BindingBuilder.bind(criticalAlertQueue).to(alertsExchange).with(ROUTING_KEY);
    }

    // ── Serialization ────────────────────────────────────────────────────────

    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter jackson2JsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jackson2JsonMessageConverter);
        return template;
    }
}
